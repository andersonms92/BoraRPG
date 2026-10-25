import java.io.File
import org.gradle.api.artifacts.ProjectDependency

data class ArchitectureImport(val name: String, val line: Int)

data class ArchitectureSource(
    val modulePath: String,
    val sourceSet: String,
    val file: File,
    val sourceRoot: File,
    val packageName: String?,
    val imports: List<ArchitectureImport>,
)

fun String.isInPackage(packageName: String) = this == packageName || startsWith(packageName + ".")

fun String.isInProject(projectPath: String) = this == projectPath || startsWith(projectPath + ":")

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

val architectureModules = subprojects.filter { it.buildFile.isFile }

val checkArchitecture =
    tasks.register("checkArchitecture") {
        group = "verification"
        description = "Valida dependências, imports e organização dos pacotes Kotlin."

        doLast {
            val violations = mutableSetOf<String>()
            val packagePattern = Regex("""(?m)^\s*package\s+([\w.]+)""")
            val importPattern =
                Regex("""^\s*import\s+(\w+(?:\.\w+)*(?:\.\*)?)(?:\s+as\s+\w+)?\s*$""")
            val sources = architectureModules.flatMap { module ->
                listOf("main", "test", "androidTest").flatMap { sourceSet ->
                    listOf("java", "kotlin").flatMap { language ->
                        val sourceRoot = module.file("src/" + sourceSet + "/" + language)
                        if (!sourceRoot.isDirectory) {
                            emptyList()
                        } else {
                            sourceRoot
                                .walkTopDown()
                                .filter { it.isFile && it.extension == "kt" }
                                .map { file ->
                                    val text = file.readText()
                                    ArchitectureSource(
                                        modulePath = module.path,
                                        sourceSet = sourceSet,
                                        file = file,
                                        sourceRoot = sourceRoot,
                                        packageName =
                                            packagePattern.find(text)?.groupValues?.get(1),
                                        imports =
                                            text
                                                .lineSequence()
                                                .mapIndexedNotNull { index, line ->
                                                    importPattern.find(line)?.let { match ->
                                                        ArchitectureImport(
                                                            match.groupValues[1],
                                                            index + 1,
                                                        )
                                                    }
                                                }
                                                .toList(),
                                    )
                                }
                                .toList()
                        }
                    }
                }
            }

            val packageOwners =
                (sources
                        .filter { it.sourceSet == "main" }
                        .mapNotNull { source ->
                            source.packageName?.let { it to source.modulePath }
                        } +
                        architectureModules.map { module ->
                            "com.borarpg" + module.path.replace(':', '.') to module.path
                        })
                    .distinct()
                    .sortedByDescending { it.first.length }

            for (source in sources) {
                val path = source.file.relativeTo(rootProject.projectDir).invariantSeparatorsPath
                val actualDirectory =
                    source.file
                        .relativeTo(source.sourceRoot)
                        .parentFile
                        ?.invariantSeparatorsPath
                        .orEmpty()
                val declaredDirectory = source.packageName?.replace('.', '/')
                if (declaredDirectory == null) {
                    violations += path + ": declaração de package ausente."
                } else if (actualDirectory != declaredDirectory) {
                    violations +=
                        path +
                            ": package " +
                            source.packageName +
                            " exige o diretório " +
                            declaredDirectory +
                            " dentro de " +
                            source.sourceRoot
                                .relativeTo(rootProject.projectDir)
                                .invariantSeparatorsPath +
                            "."
                }

                for (imported in source.imports) {
                    val location = path + ":" + imported.line
                    if (imported.name.endsWith(".*")) {
                        violations +=
                            location +
                                ": substitua o import wildcard " +
                                imported.name +
                                " por imports explícitos."
                    }

                    if (source.modulePath == ":core:domain") {
                        val platformImport =
                            listOf(
                                    "android",
                                    "androidx",
                                    "dagger",
                                    "javax.inject",
                                    "jakarta.inject",
                                    "kotlinx.coroutines.android",
                                )
                                .any { imported.name.isInPackage(it) }
                        val externalProjectImport =
                            imported.name.isInPackage("com.borarpg") &&
                                !imported.name.isInPackage("com.borarpg.domain")
                        if (platformImport || externalProjectImport) {
                            violations +=
                                location +
                                    ": import " +
                                    imported.name +
                                    " não permitido no domínio Kotlin puro."
                        }
                    }

                    val owner =
                        packageOwners.firstOrNull { imported.name.isInPackage(it.first) }?.second
                    val reason = owner?.let {
                        architectureDependencyViolation(source.modulePath, it)
                    }
                    if (reason != null) {
                        violations +=
                            location +
                                ": import " +
                                imported.name +
                                " de " +
                                owner +
                                " não permitido em " +
                                source.modulePath +
                                ". " +
                                reason
                    }
                }
            }

            for (module in architectureModules) {
                for (configuration in module.configurations) {
                    for (dependency in
                        configuration.dependencies.withType(ProjectDependency::class.java)) {
                        val reason = architectureDependencyViolation(module.path, dependency.path)
                        if (reason != null) {
                            violations +=
                                module.path +
                                    " [" +
                                    configuration.name +
                                    "]: dependência de " +
                                    dependency.path +
                                    " não permitida. " +
                                    reason
                        }
                    }
                }
            }

            if (violations.isNotEmpty()) {
                throw GradleException(
                    "Violações de arquitetura (" +
                        violations.size +
                        "):\n" +
                        violations.sorted().joinToString("\n") { " - " + it }
                )
            }
            logger.lifecycle(
                "Arquitetura validada: " +
                    architectureModules.size +
                    " módulos e " +
                    sources.size +
                    " arquivos Kotlin."
            )
        }
    }

allprojects {
    tasks.matching { it.name == "check" }.configureEach { dependsOn(checkArchitecture) }
}

tasks.named("check") {
    dependsOn(architectureModules.map { it.path + ":check" })
}
