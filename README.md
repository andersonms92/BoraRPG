# BoraRPG

App Android de aventura por movimento, em Kotlin, Jetpack Compose e MVVM. Dados locais em Room e DataStore.

## Executar

Abra no Android Studio e execute o módulo app. Requer SDK 37 e JDK compatível com o Gradle configurado.

```powershell
.\gradlew.bat :core:domain:test :app:assembleDebug lintDebug
```

Os testes instrumentados usam o runner AndroidJUnitRunner. Execute-os selecionando um emulador ou aparelho de teste.

## Atividade

A aba Atividade permite escolher caminhada ou corrida, iniciar, pausar, retomar, cancelar e finalizar. O mapa mostra o percurso GPS, a posição e os objetivos; o painel mostra tempo ativo, distância e kcal estimadas. Após finalizar, escolha Salvar resultado ou Descartar. Os percursos salvos podem ser consultados na mesma aba, inclusive após reiniciar o app.

O serviço em primeiro plano mantém o registro com a tela bloqueada. Pausar interrompe tempo, GPS e contagem de passos; retomar cria um novo segmento, sem ligar o deslocamento durante a pausa. Cancelar descarta o resultado e o percurso, preservando os passos e recompensas já registrados na jornada. Uma interrupção do processo deixa a sessão recuperável como pausada, a partir do último registro persistido; ela não reinicia automaticamente.

A distância vem do GPS, com descarte de pontos antigos, imprecisos, deslocamentos mínimos e saltos incompatíveis com a modalidade. Lacunas de mais de um minuto abrem outro segmento. Isso pode subestimar trechos sem sinal; precisão e consumo de bateria precisam de validação em aparelho físico, ao ar livre.

A localização precisa e o GPS ativo são necessários para iniciar. O sensor de passos é opcional para o percurso; sem ele, missões por passos não avançam. Não há geração artificial de passos ou distância.

## Calorias e perfil

Nome, peso, altura e idade são validados e salvos no aparelho. Cada atividade conserva o peso informado no momento do início. Sem peso válido, a estimativa de calorias fica indisponível.

A estimativa usa MET × peso em kg × horas ativas: 3,8 MET para caminhada e 8,5 MET para corrida. São intensidades de referência, sem medição individual de esforço; inclinação, ritmo e condicionamento alteram o gasto real. O app mostra kcal totais e médias por metro e quilômetro do percurso registrado. Sem deslocamento registrado, mostra zero. Referências: [Compêndio de atividades físicas](https://pacompendium.com/), [caminhada](https://pacompendium.com/walking/) e [corrida](https://pacompendium.com/running/).

## Objetivos e RPG

Informe um CEP na aba Atividade para gerar seis encontros virtuais em até 1 km do centro aproximado. Novos objetivos sorteia novamente posições, metas e recompensas de raridade comum, rara ou épica. Os marcadores mostram XP, ouro e itens; não dependem de uma consulta de pontos de interesse externos. Salve um encontro para mantê-lo no catálogo ou aceite uma missão, que permanece ativa após reiniciar.

Aceitar uma missão inicia uma meta de passos futuros. Os passos reais registrados durante atividades avançam a meta. Ao concluí-la, resgate XP, ouro e um item na mochila; o banco impede resgate duplicado. Há uma missão ativa por vez. Os encontros são virtuais: não é necessário entrar no terreno indicado pelo marcador.

Cada passo registrado também causa dano no combate da jornada; cada dez passos concedem um XP. Uma vitória concede 50 XP, 25 de ouro e um fragmento. A primeira leitura do sensor estabelece a referência; passos anteriores à atividade ou feitos durante pausas não entram na contagem.

## Mapa e dados

MapLibre Native e OpenFreeMap renderizam o mapa online. O GPS ou a busca de CEP pela BrasilAPI centralizam a região; coordenadas de CEP são aproximadas e nem todo endereço possui coordenadas. Segure o mapa para salvar um local pessoal.

Perfil, progresso, missões, inventário e percursos ficam no aparelho. Não há API própria ou sincronização entre dispositivos. O provedor de mapas recebe a região visualizada e a BrasilAPI recebe o CEP consultado. Não há mais consultas à Overpass API. A localização de visualização do mapa só é coletada enquanto a tela está aberta; durante uma atividade, o serviço mantém a coleta GPS até pausar ou finalizar.

A migração Room 1→2 remove apenas o progresso simulado antigo. As migrações 2→3 e 3→4 acrescentam missões, inventário e atividades, preservando o progresso real.

## Módulos e injeção de dependências

| Módulo | Responsabilidade |
| --- | --- |
| `app` | Application, Activity e navegação entre as features |
| `feature:journey` | Jornada, combate e resumo do personagem |
| `feature:activity` | Mapa, CEP, objetivos salvos, controles e histórico de exercícios |
| `feature:backpack` | Mochila e equipamentos |
| `feature:profile` | Formulário e validação dos dados pessoais |
| `feature:missions` | Estado e componentes de missões compartilhados pelas features |
| `core:domain` | Kotlin/JVM puro: modelos, validações, relógio, distância, calorias e regras de RPG |
| `core:data` | Room, DataStore, repositórios, consulta de CEP e localização |
| `core:tracking` | Serviço de GPS, contador de passos e controle das sessões |
| `core:designsystem` | Tema, componentes visuais e arte compartilhados |
| `build-logic` | Plugins de convenção para bibliotecas Android, Compose, Hilt e features |

As versões das dependências ficam em `gradle/libs.versions.toml`. O KSP 2.3.6 inclui a [correção de bloqueio de JARs em recompilações no Windows](https://github.com/google/ksp/releases/tag/2.3.6). Cada feature possui seu próprio build Gradle, pacote, telas e ViewModels. Jornada, Atividade e Mochila usam a feature Missões; as demais features não dependem umas das outras. Nenhum módulo `core` depende de `feature` ou de `app`.

O Hilt inicializa a aplicação (`@HiltAndroidApp`), a Activity e o serviço (`@AndroidEntryPoint`). Os ViewModels usam `@HiltViewModel`, injeção pelo construtor e `hiltViewModel()` nas rotas Compose. A feature Atividade observa exercícios através do `WorkoutRepository`, que converte os registros Room em `WorkoutSession` do domínio. O ViewModel e as telas não recebem DAOs, entidades Room ou JSON de percursos. Salvar e descartar usam o ID e transições condicionais no banco, preservando os dados mais recentes da sessão.

Repositórios e controlador de tracking são singletons; o módulo de banco fornece uma única instância Room e os DAOs. Não há factory manual ou localizador global de banco.

Os arquivos do banco e do DataStore e a versão 4 do esquema permanecem os mesmos. Modularizar não apaga nem migra novamente o progresso existente. Os esquemas e testes de persistência agora ficam em `core/data`.

## Validação

```powershell
.\gradlew.bat :core:domain:test :app:assembleDebug lintDebug
.\gradlew.bat :app:assembleDebugAndroidTest :core:data:assembleDebugAndroidTest
```

Com um emulador ou aparelho de teste selecionado:

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest :core:data:connectedDebugAndroidTest
```

Os 30 testes JVM do domínio cobrem combate, recompensas, perfil, CEP, geração de objetivos, sensor, pausas, distância e calorias. Os testes instrumentados de dados verificam persistência do percurso após reabrir o banco, recuperação de sessão, migração 2→4 e resgate único de recompensas. Os testes do `WorkoutRepository` verificam o mapeamento para o domínio, as transições permitidas e a preservação dos dados recentes diante de ações antigas da tela. O teste instrumentado do aplicativo verifica a criação dos ViewModels de todas as features através do Hilt na Activity real.

Referências: [Hilt e Gradle](https://dagger.dev/hilt/gradle-setup), [ViewModels com Hilt](https://dagger.dev/hilt/view-model.html) e [Hilt em aplicações com vários módulos](https://developer.android.com/training/dependency-injection/hilt-multi-module).
