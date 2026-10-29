# Arquitetura

O projeto usa Clean Architecture para direcionar as dependências, MVVM para a apresentação e fluxo unidirecional de ações e estado na Atividade. Os módulos definem limites de compilação; os pacotes internos separam responsabilidades. As features contêm apresentação, enquanto domínio e implementações de dados ficam nos módulos compartilhados.

## Dependências e composição

```mermaid
flowchart TD
    app --> features[feature:*]
    app --> data[core:data]
    app --> tracking[core:tracking]
    app --> design[core:designsystem]
    features --> domain[core:domain]
    features --> design
    data --> domain
    tracking --> domain
```

As setas representam dependências de compilação. `core` é um agrupamento de diretórios: `core:domain` e `core:data` possuem builds e limites de compilação independentes. O domínio não conhece as implementações. Jornada, Atividade e Mochila também dependem de `feature:missions` para reutilizar sua apresentação.

Os ViewModels recebem casos de uso de domínio. Estes acessam contratos de repositório, implementados exclusivamente em dados. Hilt conecta interface e implementação com `@Binds`; `app/di/UseCaseModule` constrói os casos de uso sem introduzir injeção no domínio. Não há necessidade de uma segunda interface Bridge que duplique o contrato do repositório.

```text
Screen → ação → ViewModel → caso de uso → Repository (contrato do domínio)
                                             ↑ implementado por
                                      RepositoryImpl (data)
                                             ↓
                                    Room / DataStore / Retrofit
```

O acesso a `WorkoutTracker` também passa por um contrato de domínio. As features não importam implementações, banco, DAOs, entidades ou transporte de rede. As classes de infraestrutura de dados são `internal`, inclusive quando o módulo `app` inclui `core:data` para a composição Hilt.

## Organização dos módulos

Os diretórios abaixo representam pacotes existentes sob `src/main/java`. Testes ficam nos respectivos `src/test` e `src/androidTest`, acompanhando o pacote da responsabilidade testada.

```text
app
├── BoraApplication.kt
├── MainActivity.kt
└── navigation
    ├── AdventureApp.kt
    ├── AppDestination.kt
    └── AppNavigationBar.kt

core:domain
├── model
│   ├── hero
│   ├── inventory
│   ├── location
│   ├── mission
│   ├── player
│   └── workout
├── repository
├── usecase
│   ├── hero
│   ├── location
│   ├── mission
│   ├── player
│   ├── quest
│   └── workout
├── rules
│   ├── combat
│   ├── location
│   ├── mission
│   └── workout
├── tracking
└── error

core:data
├── local
│   ├── database
│   ├── dao
│   ├── entity
│   ├── migration
│   └── preferences
├── remote/cep
├── location
├── mapper
├── repository
└── di

core:tracking
├── service
├── controller
├── state
├── time
├── notification
└── di

core:designsystem
├── theme
├── components
└── illustration
```

| Feature | Pacotes sob `presentation` |
| --- | --- |
| `journey` | `screen`, `components`, `state`, `viewmodel` |
| `activity` | `screen`, `components`, `map`, `permissions`, `formatter`, `action`, `state`, `viewmodel` |
| `backpack` | `screen`, `components`, `viewmodel` |
| `profile` | `screen`, `components`, `form`, `state`, `viewmodel` |
| `missions` | `components`, `state`, `viewmodel` |

`build-logic/src/main/kotlin` reúne os plugins de convenção Android library, Compose, Hilt e feature. As versões são centralizadas em `gradle/libs.versions.toml`.

## Domínio

`core:domain` usa Kotlin/JVM e coroutines, sem Android, Room, Retrofit ou Hilt. `model` contém tipos de negócio; `repository` define contratos de armazenamento e consulta; `usecase` define as operações consumidas pela apresentação; `tracking` define `WorkoutTracker` e `WorkoutTimeSource`; `rules` reúne cálculos e coordenação; `error` contém a exceção de consulta de CEP.

Todos os ViewModels usam casos de uso para consultar ou alterar dados. `FindCepUseCase` valida e normaliza o CEP; `CreateQuestUseCase` valida os dados do objetivo, limpa o título e recebe uma fábrica de IDs; `GenerateObjectivesUseCase` encapsula a geração aleatória. `ObserveMissionsUseCase` reúne progresso, inventário e resgates em `MissionOverview`. Salvar perfil e aceitar missão também validam o domínio antes da persistência.

`QuestPoint` exige título não vazio de até 60 caracteres e coordenadas válidas. `PlayerProfile` mantém medidas numéricas e seus limites. `MissionRules` valida os requisitos de uma missão gerada e calcula a recompensa somente quando a meta foi concluída. Essas regras independem do banco.

`WorkoutType` contém `WALK` e `RUN`, sem rótulos de apresentação. A duração permanece numérica no domínio; `formatActivityTime` e os nomes exibidos das modalidades pertencem a `feature:activity/presentation/formatter`.

`WorkoutSessionManager` coordena iniciar, pausar, retomar, finalizar e cancelar. Recebe os contratos de repositório, relógio e gerador de ID. Usa tempo monotônico para duração e tempo civil para o início. Retomar reinicia a referência do sensor e cria um segmento; deslocamentos entre segmentos não entram na distância. Finalizar produz um rascunho, que pode ser salvo ou descartado separadamente.

O serviço chama seus métodos serialmente. O gerenciador publica a transição após persistir o checkpoint; sessões interrompidas podem ser recuperadas como pausadas. `WorkoutClock`, `StepCounter`, os filtros de GPS e os cálculos de distância e calorias também ficam no domínio. Os casos de uso estabelecem a fronteira de acesso da apresentação; somente eles, as regras de domínio e a composição Hilt recebem contratos de repositório fora de dados.

## Dados e compatibilidade

`local/database` contém `GameDatabase`; `dao` contém consultas; `entity` contém as representações Room; `migration` contém as migrações; `preferences` contém as chaves DataStore e o codec dos objetivos. Conversões entre persistência, resposta remota e domínio ficam em `mapper`.

`repository` implementa os contratos de domínio com classes `internal` terminadas em `RepositoryImpl`. `AdventureRepositoryImpl` e `MissionRepositoryImpl` executam regras e atualizações na mesma transação. No resgate, experiência, ouro, inventário e estado da missão são confirmados juntos; o repositório também impede uma segunda missão ativa e resgate duplicado. As regras de recompensa permanecem em `MissionRules`.

`PlayerRepositoryImpl` armazena perfil e objetivos pessoais no DataStore. `LocationRepositoryImpl` adapta a localização de visualização para os tipos do domínio. `di` fornece banco, dispatchers e vínculos das implementações.

A busca de CEP segue `CepViewModel → FindCepUseCase → CepRepository → CepRepositoryImpl → CepRemoteDataSource → CepApi`. `CepApi` é uma interface Retrofit com `@GET`; `NetworkModule` configura Retrofit, OkHttp e Moshi. DTOs Moshi gerados por KSP representam o JSON aninhado da BrasilAPI e permanecem internos a dados. O mapper converte a resposta para `CepLocation`, validando coordenadas. Não há `HttpURLConnection` ou JSON manual no transporte de CEP.

`CepLookupError` enumera os motivos de falha sem códigos HTTP ou mensagens de apresentação. `CepLookupException` transporta o motivo e preserva a causa técnica. `CepErrorMapper`, em dados, centraliza a classificação HTTP/JSON/conexão usando constantes HTTP nomeadas. Cancelamento e exceções inesperadas são propagados. `CepErrorFormatter`, na apresentação, traduz o motivo para a mensagem da tela; uma falha inesperada recebe uma mensagem neutra. O enum é suficiente porque os motivos não possuem payloads diferentes. `CepState` continua como data class para representar campos simultâneos da tela.

`WorkoutRepositoryImpl` mantém resumos em `workouts` e pontos em `workout_points`. Checkpoints inserem apenas pontos novos e atualizam o resumo na mesma transação. Um lote inválido reverte a operação; excluir a atividade remove os pontos por chave estrangeira. O histórico contém resumos com `route` vazio. `ObserveWorkoutsUseCase` fornece o histórico e `ObserveWorkoutRouteUseCase` observa o percurso selecionado. `current()` é usado pelo gerenciador de domínio para retomar a sessão.

`AdventureRepository.recordSteps` retorna `GuardianDefeat` somente quando o lote gera novas vitórias. `AdventureRepositoryImpl` calcula o evento dentro da transação e o retorna após confirmar passos e recompensas. Consultar o progresso existente não gera eventos. `LevelProgression` calcula o nível a partir do XP total, com requisitos progressivos; alterar a curva não modifica o progresso persistido.

`Combat.GUARDIAN_HEALTH` define os 5.000 pontos de vida de cada guardião, usados no cálculo do combate e na apresentação. Cada passo causa um ponto de dano; lotes podem derrotar mais de um guardião, preservando o dano excedente no próximo encontro. As recompensas continuam sendo 50 XP e 25 de ouro por vitória, além do XP por passos.

`QuestKind`, `QuestRarity` e o filtro de apresentação `QuestFilter` substituem categorias textuais. Rótulos como “Combate” e “Comum” ficam na apresentação. `QuestCategoryMapper` converte códigos estáveis de armazenamento e aceita os rótulos antigos em português, incluindo diferenças de maiúsculas/minúsculas. Valores desconhecidos ou ausentes usam alternativas explícitas. Títulos, nomes, descrições e IDs continuam texto livre. Os tipos/status serializados das entidades pertencem à fronteira de persistência e são convertidos para os enums do domínio.

Salvar e descartar retornam `APPLIED`, `NOT_FOUND` ou `INVALID_STATE`. As consultas condicionais impedem alterações incompatíveis e checkpoints tardios sobre resultados finalizados.

O arquivo continua sendo `borarpg.db`, na versão **6**, com FQN `com.borarpg.data.local.database.GameDatabase` e schemas exportados em `core/data/schemas/com.borarpg.data.local.database.GameDatabase`. Os schemas históricos 1 a 5 permanecem com o mesmo conteúdo. A migração 4→5 continua convertendo os percursos JSON em pontos ordenados. A migração 5→6 acrescenta 4.000 à vida restante do guardião, preservando passos, dano já causado, vitórias, XP, ouro e demais registros. Essa atualização ocorre uma única vez pelo controle de versão do Room e não produz eventos de derrota. Como a estrutura das tabelas não muda, a identidade do schema pode permanecer igual à da versão 5.

## Integração Android

`service/StepTrackingService` mantém notificação, GPS e sensor. Converte callbacks Android em `TrackingCommand`, processados em sequência; o gerenciador de domínio decide as mudanças de sessão. O serviço recebe `WorkoutSessionManager` pronto, sem receber repositórios, DAOs ou entidades. `WorkoutSessionModule` fornece o gerenciador com `@ServiceScoped`, uma instância por serviço.

Uma derrota confirmada por `WorkoutSessionManager.steps` é entregue a `notification/GuardianVictoryNotifier`. O adaptador publica o aviso em um canal próprio, com ID diferente do serviço, respeitando a permissão de notificações. Lotes com várias vitórias produzem um aviso agregado. Falhas ao publicar o aviso não interrompem a atividade; eventos históricos não são reproduzidos.

`controller/TrackingController` implementa `WorkoutTracker` e envia comandos ao serviço após executar `RecoverInterruptedWorkoutsUseCase`. `state/TrackingRuntime` compartilha o estado durante a vida do processo pelo Hilt. `time/AndroidWorkoutTimeSource` fornece os relógios da plataforma, e `di/TrackingModule` conecta os contratos. O banco permite recuperar uma interrupção do processo; o serviço usa `START_NOT_STICKY`.

## Apresentação e navegação

As composables `Route` obtêm ViewModels pelo Hilt, coletam seus estados com o ciclo de vida e conectam callbacks. As `Screen` recebem estados e ações, sem receber ViewModels. Componentes menores recebem somente os dados e callbacks necessários.

Na Atividade, `ActivityRoute` combina exercício, missões, CEP, locais salvos e permissões em `ActivityUiState`. `ActivityScreen` envia `ActivityAction`; a Route encaminha comandos aos ViewModels e abre configurações da plataforma quando necessário. Estado visual local do mapa e diálogos permanece no Compose.

O fluxo dos exercícios é `WorkoutAction → WorkoutViewModel → WorkoutUiState → Compose`. O ViewModel combina resumos persistidos, percurso selecionado, tracking e escolhas da interface. O percurso é associado ao ID para evitar exibir dados de uma consulta anterior em outra atividade. Operações incompatíveis e salvamentos simultâneos são bloqueados; o gerenciador valida novamente as transições. A tela observa a sessão existente, inclusive quando sua execução ocorre em segundo plano.

O perfil também é observado para estimar kcal de atividades antigas sem peso registrado. `resolveWorkoutWeight` prioriza o peso original e permite usar o peso atual como alternativa explicitamente identificada na interface, sem reescrever a atividade. As médias por metro e quilômetro usam tempo ativo, modalidade e distância do resultado exibido.

`permissions` adapta solicitações de localização e atividade física; `map` encapsula MapLibre e a conversão de coordenadas; `formatter` prepara duração e nomes para exibição. `WorkoutPanel` e `ObjectiveDetails` recebem estado e callbacks. A expansão do painel é estado visual local salvo pelo Compose. `WorkoutControls` mantém o launcher de permissões e `WorkoutDialogs` compostos fora da área animada; recolher o painel não interrompe callbacks, diálogos ou a sessão.

As outras features mantêm MVVM com métodos diretos. `ProfileForm` representa textos em edição e converte vírgula decimal para as medidas numéricas de `PlayerProfile`. `feature:missions` fornece estado e componentes compartilhados por Jornada, Atividade e Mochila.

Em `app/navigation`, `AppDestination` representa as abas como enum, substituindo índices numéricos. `AdventureApp` mantém a seleção com `rememberSaveable` e apresenta a Route correspondente; `AppNavigationBar` renderiza a seleção. `BoraApplication` e `MainActivity` são os pontos de entrada Hilt.

`core:designsystem` concentra cores e `BoraTheme` em `theme`, componentes como `Eyebrow` e `Tag` em `components` e a arte `GuardianArt` em `illustration`. Não depende das features nem contém regras de negócio.

## Verificação de arquitetura e testes

`gradle/architecture.gradle.kts` define `checkArchitecture`, também executada pelas tarefas `check`. A verificação inspeciona dependências Gradle e arquivos Kotlin de `main`, `test` e `androidTest`:

- Domínio não pode depender de outro módulo nem importar Android, AndroidX, Hilt, injeção ou coroutines Android.
- Dados e tracking só podem depender de seus próprios tipos e dos contratos de domínio.
- Design system não pode depender de outros módulos do aplicativo.
- Features podem usar domínio, design system e a apresentação compartilhada de missões; outros módulos de infraestrutura e features não são permitidos.
- O diretório de cada arquivo deve corresponder ao pacote declarado.
- Todos os imports devem ser explícitos, sem wildcard.
- Implementações de dados não podem ser referenciadas fora de `core:data`, inclusive pelo `app`.
- Bibliotecas de banco, preferências e rede não podem ser usadas na apresentação ou no domínio.
- A apresentação e os adaptadores Android consomem casos de uso; contratos de repositório ficam restritos a domínio, dados e composição DI.
- Referências totalmente qualificadas e imports com alias também são verificados; comentários e literais não contam como código.

`checkArchitectureRules` exercita cenários positivos e negativos das próprias regras antes da inspeção do projeto.

A suíte JVM exercita regras, invariantes, sessão, formulário, formatação e ViewModels. Os testes instrumentados verificam persistência, migrações até a versão 6, preservação do progresso na mudança de dificuldade e após reabrir o banco, atomicidade, cascata, resgate único e composição Hilt. Os comandos estão no [README](../README.md). Precisão do GPS, bateria e execução prolongada também exigem validação em aparelho físico.
