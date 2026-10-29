import java.io.File
import org.gradle.api.artifacts.ProjectDependency

data class ArchitectureReference(val name: String, val line: Int, val imported: Boolean)

data class ArchitectureSource(
    val modulePath: String,
    val sourceSet: String,
    val file: File,
    val sourceRoot: File,
    val packageName: String?,
    val references: List<ArchitectureReference>,
)

fun String.isInPackage(packageName: String) = this == packageName || startsWith(packageName + ".")

fun String.isInProject(projectPath: String) = this == projectPath || startsWith(projectPath + ":")

fun String.isInAnyPackage(packages: List<String>): Boolean {
    for (packageName in packages) {
        if (isInPackage(packageName)) return true
    }
    return false
}

fun architectureDependencyViolation(source: String, target: String): String? =
    when {
        target == source -> null
        source == ":core:domain" -> "O domínio não pode depender de outros módulos."
        source == ":core:data" && target != ":core:domain" ->
            "Dados podem depender apenas do próprio módulo e de core:domain."
        source == ":core:tracking" && target != ":core:domain" ->
            "O acompanhamento pode depender apenas do próprio módulo e de core:domain."
        source == ":core:designsystem" -> "O design system não pode depender de outros módulos."
        source.isInProject(":feature") &&
            target !in setOf(":core:domain", ":core:designsystem", ":feature:missions") ->
            "Features podem depender apenas do próprio módulo, de core:domain, " +
                "de core:designsystem e da apresentação compartilhada feature:missions."
        else -> null
    }

fun architectureLayerViolation(
    modulePath: String,
    packageName: String?,
    sourceSet: String,
    reference: String,
): String? {
    if (modulePath != ":core:data" && reference.isInPackage("com.borarpg.data")) {
        return "Detalhes de dados são internos a core:data; use os casos de uso do domínio."
    }
    val infrastructure =
        listOf(
            "androidx.room",
            "androidx.datastore",
            "androidx.sqlite",
            "retrofit2",
            "okhttp3",
            "okio",
            "com.squareup.moshi",
            "org.json",
            "java.net.HttpURLConnection",
            "java.net.URLConnection",
            "java.net.URL",
            "java.net.http",
        )
    if (modulePath != ":core:data" && reference.isInAnyPackage(infrastructure)) {
        return "Persistência, DTOs e transporte HTTP pertencem exclusivamente a core:data."
    }
    if (modulePath == ":core:domain") {
        val platform =
            listOf(
                "android",
                "androidx",
                "dagger",
                "javax.inject",
                "jakarta.inject",
                "kotlinx.coroutines.android",
            )
        val externalModule =
            reference.isInPackage("com.borarpg") && !reference.isInPackage("com.borarpg.domain")
        if (reference.isInAnyPackage(platform) || externalModule) {
            return "O domínio deve permanecer Kotlin puro, sem infraestrutura ou DI."
        }
    }
    if (sourceSet == "main" && reference.isInPackage("com.borarpg.domain.repository")) {
        val owner = packageName.orEmpty()
        val allowed =
            modulePath == ":core:data" ||
                modulePath == ":core:domain" &&
                    owner.isInAnyPackage(listOf(
                            "com.borarpg.domain.repository",
                            "com.borarpg.domain.usecase",
                            "com.borarpg.domain.rules",
                        )) ||
                modulePath == ":app" && owner.isInPackage("com.borarpg.di") ||
                modulePath == ":core:tracking" && owner.isInPackage("com.borarpg.tracking.di")
        if (!allowed) {
            return "A apresentação e os adaptadores consultam casos de uso, não repositories."
        }
    }
    return null
}

fun architectureCode(source: String): String {
    val code = StringBuilder(source.length)
    var index = 0
    fun hide() {
        val character = source[index++]
        code.append(if (character == '\n' || character == '\r') character else ' ')
    }
    while (index < source.length) {
        when {
            source.startsWith("//", index) -> {
                while (index < source.length && source[index] != '\n') hide()
            }
            source.startsWith("/*", index) -> {
                hide()
                hide()
                var depth = 1
                while (index < source.length && depth > 0) {
                    when {
                        source.startsWith("/*", index) -> {
                            depth++
                            hide()
                            hide()
                        }
                        source.startsWith("*/", index) -> {
                            depth--
                            hide()
                            hide()
                        }
                        else -> hide()
                    }
                }
            }
            source.startsWith("\"\"\"", index) -> {
                repeat(3) { hide() }
                while (index < source.length && !source.startsWith("\"\"\"", index)) hide()
                if (index < source.length) repeat(3) { hide() }
            }
            source[index] == '\"' || source[index] == '\'' -> {
                val delimiter = source[index]
                hide()
                while (index < source.length) {
                    val character = source[index]
                    hide()
                    if (character == '\\' && index < source.length) hide()
                    else if (character == delimiter) break
                }
            }
            else -> code.append(source[index++])
        }
    }
    return code.toString()
}

fun architectureReferences(code: String): List<ArchitectureReference> {
    val imports = Regex("""(?m)^[\t ]*import[\t ]+([\w.]+(?:\.\*)?)(?:[\t ]+as[\t ]+\w+)?[\t ]*$""")
    val declarations = Regex("""(?m)^[\t ]*(?:package|import)[\t ]+[^\r\n]*""")
        .findAll(code)
        .map { it.range }
        .toList()
    val qualified = Regex("""\b[A-Za-z_]\w*(?:\s*\.\s*[A-Za-z_]\w*)+(?:\s*\.\s*\*)?""")
    fun lineAt(offset: Int) = code.take(offset).count { it == '\n' } + 1
    val imported = imports.findAll(code).map {
        ArchitectureReference(it.groupValues[1], lineAt(it.range.first), imported = true)
    }
    val inline = qualified.findAll(code)
        .filter { match -> declarations.none { match.range.first in it } }
        .map {
            ArchitectureReference(
                it.value.filterNot(Char::isWhitespace),
                lineAt(it.range.first),
                imported = false,
            )
        }
    return (imported + inline).toList().distinct()
}

val architectureModules = subprojects.filter { it.buildFile.isFile }

val checkArchitectureRules = tasks.register("checkArchitectureRules") {
    group = "verification"
    description = "Testa as barreiras de arquitetura e a leitura de referências Kotlin."
    doLast {
        fun violation(module: String, owner: String, reference: String, sourceSet: String = "main") =
            architectureLayerViolation(module, owner, sourceSet, reference)
        val feature = "com.borarpg.feature.activity.presentation.viewmodel"
        check(violation(":feature:activity", feature, "com.borarpg.data.local.dao.WorkoutDao") != null)
        check(violation(":app", "com.borarpg.di", "com.borarpg.data.repository.WorkoutRepositoryImpl") != null)
        check(violation(":feature:activity", feature, "androidx.room.Room") != null)
        check(violation(":feature:profile", feature, "androidx.datastore.core.DataStore") != null)
        check(violation(":core:tracking", "com.borarpg.tracking.service", "retrofit2.Retrofit") != null)
        check(violation(":app", "com.borarpg", "okhttp3.OkHttpClient") != null)
        check(violation(":core:domain", "com.borarpg.domain.usecase", "javax.inject.Inject") != null)
        check(violation(":feature:activity", feature, "com.borarpg.domain.repository.WorkoutRepository") != null)
        check(violation(":core:tracking", "com.borarpg.tracking.controller", "com.borarpg.domain.repository.WorkoutRepository") != null)
        check(violation(":core:domain", "com.borarpg.domain.usecase.workout", "com.borarpg.domain.repository.WorkoutRepository") == null)
        check(violation(":core:domain", "com.borarpg.domain.rules.workout", "com.borarpg.domain.repository.WorkoutRepository") == null)
        check(violation(":app", "com.borarpg.di", "com.borarpg.domain.repository.WorkoutRepository") == null)
        check(violation(":core:tracking", "com.borarpg.tracking.di", "com.borarpg.domain.repository.WorkoutRepository") == null)
        check(violation(":feature:activity", feature, "com.borarpg.domain.repository.WorkoutRepository", "test") == null)
        check(violation(":core:data", "com.borarpg.data.repository", "androidx.room.Room") == null)
        check(architectureDependencyViolation(":core:domain", ":core:data") != null)
        check(architectureDependencyViolation(":feature:activity", ":core:data") != null)
        check(architectureDependencyViolation(":core:data", ":core:domain") == null)
        val snippet = listOf(
            "package com.borarpg.feature.activity",
            "import com.borarpg.domain.repository.WorkoutRepository as Store",
            "val db = com.borarpg.data.local.database.GameDatabase::class",
            "val client = retrofit2 . Retrofit.Builder()",
            "// com.borarpg.data.local.dao.HeroDao",
            "/* outer /* androidx.room.Room */ okhttp3.OkHttpClient */",
            "val text = \"androidx.datastore.core.DataStore\"",
            "val raw = " + "\"\"\"" + "com.borarpg.data.remote.cep.CepResponse" + "\"\"\"",
            "val escaped = \"text \\\" org.json.JSONObject\"",
            "val dot = '.'",
        ).joinToString("\n")
        val code = architectureCode(snippet)
        check(code.length == snippet.length)
        check(code.count { it == '\n' } == snippet.count { it == '\n' })
        val references = architectureReferences(code)
        check(references == listOf(
            ArchitectureReference("com.borarpg.domain.repository.WorkoutRepository", 2, imported = true),
            ArchitectureReference("com.borarpg.data.local.database.GameDatabase", 3, imported = false),
            ArchitectureReference("retrofit2.Retrofit.Builder", 4, imported = false),
        ))
        check(architectureReferences(architectureCode("import kotlin.collections.*")).single().name.endsWith(".*"))
        logger.lifecycle("Regras de arquitetura validadas: isolamento, composição, FQNs, aliases e literais.")
    }
}

val checkArchitecture = tasks.register("checkArchitecture") {
    group = "verification"
    description = "Valida dependências, camadas e organização dos pacotes Kotlin."
    dependsOn(checkArchitectureRules)
    doLast {
        val violations = mutableSetOf<String>()
        val packagePattern = Regex("""(?m)^[\t ]*package[\t ]+([\w.]+)""")
        val sources = architectureModules.flatMap { module ->
            listOf("main", "test", "androidTest").flatMap { sourceSet ->
                listOf("java", "kotlin").flatMap { language ->
                    val sourceRoot = module.file("src/$sourceSet/$language")
                    if (!sourceRoot.isDirectory) emptyList()
                    else sourceRoot.walkTopDown()
                        .filter { it.isFile && it.extension == "kt" }
                        .map { file ->
                            val code = architectureCode(file.readText())
                            ArchitectureSource(
                                module.path,
                                sourceSet,
                                file,
                                sourceRoot,
                                packagePattern.find(code)?.groupValues?.get(1),
                                architectureReferences(code),
                            )
                        }.toList()
                }
            }
        }
        val packageOwners =
            (sources.filter { it.sourceSet == "main" }.mapNotNull { source ->
                source.packageName?.let { it to source.modulePath }
            } + architectureModules.flatMap { module ->
                val packageRoot = when (module.path) {
                    ":app" -> "com.borarpg"
                    ":core:domain" -> "com.borarpg.domain"
                    ":core:data" -> "com.borarpg.data"
                    ":core:tracking" -> "com.borarpg.tracking"
                    else -> "com.borarpg" + module.path.replace(':', '.')
                }
                listOf(
                    packageRoot to module.path,
                    "com.borarpg" + module.path.replace(':', '.') to module.path,
                )
            }).distinct().sortedByDescending { it.first.length }
        for (source in sources) {
            val path = source.file.relativeTo(rootProject.projectDir).invariantSeparatorsPath
            val actualDirectory = source.file.relativeTo(source.sourceRoot).parentFile
                ?.invariantSeparatorsPath.orEmpty()
            val declaredDirectory = source.packageName?.replace('.', '/')
            if (declaredDirectory == null) {
                violations += "$path: declaração de package ausente."
            } else if (actualDirectory != declaredDirectory) {
                violations += "$path: package ${source.packageName} exige o diretório $declaredDirectory."
            }
            for (reference in source.references) {
                val location = "$path:${reference.line}"
                if (reference.imported && reference.name.endsWith(".*")) {
                    violations += "$location: substitua o import wildcard ${reference.name} por imports explícitos."
                }
                val layerReason = architectureLayerViolation(
                    source.modulePath,
                    source.packageName,
                    source.sourceSet,
                    reference.name,
                )
                if (layerReason != null) {
                    violations += "$location: referência ${reference.name} não permitida. $layerReason"
                }
                val owner = packageOwners.firstOrNull { reference.name.isInPackage(it.first) }?.second
                val reason = owner?.let { architectureDependencyViolation(source.modulePath, it) }
                if (reason != null) {
                    violations += "$location: referência ${reference.name} de $owner não permitida em ${source.modulePath}. $reason"
                }
            }
        }
        val infrastructureGroups = listOf(
            "androidx.room", "androidx.datastore", "androidx.sqlite",
            "com.squareup.retrofit2", "com.squareup.okhttp3", "com.squareup.okio",
            "com.squareup.moshi",
        )
        for (module in architectureModules) {
            for (configuration in module.configurations) {
                for (dependency in configuration.dependencies) {
                    val reason = if (dependency is ProjectDependency) {
                        architectureDependencyViolation(module.path, dependency.path)
                    } else if (module.path != ":core:data" &&
                        dependency.group.orEmpty().isInAnyPackage(infrastructureGroups)) {
                        "Bibliotecas de persistência e transporte HTTP pertencem a core:data."
                    } else null
                    if (reason != null) {
                        violations += "${module.path} [${configuration.name}]: dependência ${dependency.group}:${dependency.name} não permitida. $reason"
                    }
                }
            }
        }
        if (violations.isNotEmpty()) {
            throw GradleException(
                "Violações de arquitetura (${violations.size}):\n" +
                    violations.sorted().joinToString("\n") { " - $it" }
            )
        }
        logger.lifecycle("Arquitetura validada: ${architectureModules.size} módulos e ${sources.size} arquivos Kotlin.")
    }
}

allprojects {
    tasks.matching { it.name == "check" }.configureEach { dependsOn(checkArchitecture) }
}

tasks.named("check") {
    dependsOn(architectureModules.map { it.path + ":check" })
}
