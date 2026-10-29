# BoraRPG

App Android de aventura por movimento, em Kotlin e Jetpack Compose. MVVM com fluxo unidirecional na Atividade, contratos de domínio e injeção com Hilt. Dados locais em Room e DataStore.

## Atividade

A aba Atividade permite escolher caminhada ou corrida, iniciar, pausar, retomar, cancelar e finalizar. O mapa mostra o percurso GPS, a posição e os objetivos; o painel mostra tempo ativo, distância e kcal estimadas. Após finalizar, escolha Salvar resultado ou Descartar. Os percursos salvos podem ser consultados na mesma aba, inclusive após reiniciar o app.

O painel começa recolhido no ícone de caminhada ao lado de centralizar. Toque para expandir os controles e use a seta para recolher novamente. O ícone indica atividade em andamento, pausa, resultado pendente ou aviso. Recolher mantém GPS e cronômetro ativos; selecionar um objetivo abre seus detalhes e recompensas no início do painel. A barra flutuante reúne Buscar CEP, o ícone de atualizar para Novos objetivos, o histórico de atividades, o painel da atividade e a centralização no GPS. O ícone de relógio abre o histórico mesmo com os controles recolhidos; Ver percurso abre os dados e o mapa da atividade selecionada. Durante um exercício em andamento, a lista permite ler os resumos e orienta finalizar antes de trocar o percurso exibido.

O serviço em primeiro plano mantém o registro com a tela bloqueada. Pausar interrompe tempo, GPS e contagem de passos; retomar cria um novo segmento, sem ligar o deslocamento durante a pausa. Cancelar descarta o resultado e o percurso, preservando os passos e recompensas já registrados na jornada. Uma interrupção do processo deixa a sessão recuperável como pausada, a partir do último registro persistido; ela não reinicia automaticamente.

A distância vem do GPS, com descarte de pontos antigos, imprecisos, deslocamentos mínimos e saltos incompatíveis com a modalidade. Lacunas de mais de um minuto abrem outro segmento. Isso pode subestimar trechos sem sinal; precisão e consumo de bateria precisam de validação em aparelho físico, ao ar livre.

A localização precisa e o GPS ativo são necessários para iniciar. O sensor de passos é opcional para o percurso; sem ele, missões por passos não avançam. Não há geração artificial de passos ou distância.

## Calorias e perfil

Nome, peso, altura e idade são validados e salvos no aparelho. Cada atividade conserva o peso informado no momento do início. Se uma atividade antiga não registrou peso, o app pode estimar suas kcal e médias usando o peso atual do perfil, identificando essa origem na tela e no histórico. O peso originalmente salvo continua tendo prioridade. Sem peso válido, o cartão de kcal oferece Cadastrar peso e acesso direto à aba Você; preencher o perfil atualiza a estimativa sem alterar os dados da atividade.

A estimativa usa MET × peso em kg × horas ativas: 3,8 MET para caminhada e 8,5 MET para corrida. São intensidades de referência, sem medição individual de esforço; inclinação, ritmo e condicionamento alteram o gasto real. O app mostra kcal totais e médias por metro e quilômetro do percurso registrado. Sem deslocamento registrado, mostra zero. Referências: [Compêndio de atividades físicas](https://pacompendium.com/), [caminhada](https://pacompendium.com/walking/) e [corrida](https://pacompendium.com/running/).

## Objetivos e RPG

Informe um CEP na aba Atividade para gerar seis encontros virtuais em até 1 km do centro aproximado. Novos objetivos sorteia novamente posições, metas e recompensas de raridade comum, rara ou épica. Os marcadores mostram XP, ouro e itens; não dependem de uma consulta de pontos de interesse externos. Salve um encontro para mantê-lo no catálogo ou aceite uma missão, que permanece ativa após reiniciar.

Aceitar uma missão inicia uma meta de passos futuros. Os passos reais registrados durante atividades avançam a meta. Ao concluí-la, resgate XP, ouro e um item na mochila; o banco impede resgate duplicado. Há uma missão ativa por vez. Os encontros são virtuais: não é necessário entrar no terreno indicado pelo marcador.

Cada passo registrado também causa dano no combate da jornada; cada dez passos concedem um XP. Uma vitória concede 50 XP, 25 de ouro e um fragmento. A primeira leitura do sensor estabelece a referência; passos anteriores à atividade ou feitos durante pausas não entram na contagem.

Ao chegar a zero de vida, o guardião é derrotado e outro surge com 1.000 de vida. A Jornada mostra o número do encontro e o total de vitórias: 1.996 passos representam uma vitória e o segundo guardião com 4 de vida. Novas derrotas confirmadas no banco geram uma notificação com as recompensas; lotes de passos que vencem vários guardiões geram um único aviso agrupado. Avisos antigos não são repetidos ao reabrir o app. A exibição respeita as permissões de notificação e as configurações do Android.

A progressão começa com 1.000 XP para atingir o nível 2. Cada passagem seguinte exige 250 XP a mais: 1.250, 1.500, 1.750 e assim por diante. O XP total, ouro, itens e vitórias existentes são preservados; o nível e a barra são recalculados pela nova curva. Por exemplo, 699 XP correspondem ao nível 1, faltando 301 XP para o nível 2.

## Mapa e dados

MapLibre Native e OpenFreeMap renderizam o mapa online. O GPS ou a busca de CEP pela BrasilAPI centralizam a região; coordenadas de CEP são aproximadas e nem todo endereço possui coordenadas. Segure o mapa para salvar um local pessoal.

Perfil, progresso, missões, inventário e percursos ficam no aparelho. Não há API própria ou sincronização entre dispositivos. O provedor de mapas recebe a região visualizada e a BrasilAPI recebe o CEP consultado. Não há mais consultas à Overpass API. A localização de visualização do mapa só é coletada enquanto a tela está aberta; durante uma atividade, o serviço mantém a coleta GPS até pausar ou finalizar.

A migração Room 1→2 remove apenas o progresso simulado antigo. As migrações 2→3 e 3→4 acrescentam missões, inventário e atividades, preservando o progresso real. A migração 4→5 transfere os percursos JSON para uma tabela de pontos, preservando coordenadas, ordem, segmentos e resultados.

## Módulos e injeção de dependências

| Módulo | Responsabilidade e organização interna |
| --- | --- |
| `app` | Inicialização e `navigation`, com destinos tipados em `AppDestination` |
| `feature:journey` | Jornada e combate; `presentation/screen`, `components`, `state` e `viewmodel` |
| `feature:activity` | Mapa e exercícios; `presentation/screen`, `components`, `map`, `permissions`, `formatter`, `action`, `state` e `viewmodel` |
| `feature:backpack` | Mochila; `presentation/screen`, `components` e `viewmodel` |
| `feature:profile` | Perfil; `presentation/screen`, `components`, `form`, `state` e `viewmodel` |
| `feature:missions` | Apresentação compartilhada de missões; `presentation/components`, `state` e `viewmodel` |
| `core:domain` | Kotlin/JVM puro; `model`, `usecase`, `repository`, `rules`, `tracking` e `error` |
| `core:data` | `local/{database,dao,entity,migration,preferences}`, `remote/cep`, `location`, `mapper`, `repository` e `di` |
| `core:tracking` | Integração Android em `service`, `controller`, `state`, `time`, `notification` e `di` |
| `core:designsystem` | `theme`, `components` e `illustration`, sem regras de negócio |
| `build-logic` | Plugins de convenção para bibliotecas Android, Compose, Hilt e features |

As versões das dependências ficam em `gradle/libs.versions.toml`. O KSP 2.3.6 inclui a [correção de bloqueio de JARs em recompilações no Windows](https://github.com/google/ksp/releases/tag/2.3.6). Cada feature possui seu próprio build Gradle, pacote e apresentação. Jornada, Atividade e Mochila usam a feature Missões; as demais features não dependem umas das outras. Nenhum módulo `core` depende de `feature` ou de `app`.

O Hilt inicializa a aplicação, a Activity e o serviço. Todos os ViewModels consultam casos de uso de `core:domain`, que dependem de interfaces de repositório. `core:data` fornece classes `RepositoryImpl` internas, e `core:tracking` implementa o acompanhamento Android. Os módulos Hilt conectam os contratos às implementações; `app/di/UseCaseModule` constrói os casos de uso mantendo o domínio sem Hilt. `core:data` e `core:domain` são módulos Gradle separados, agrupados apenas pelo diretório `core`. Nenhuma feature depende diretamente de dados ou tracking.

A consulta de CEP usa `FindCepUseCase → CepRepository → CepRepositoryImpl → CepRemoteDataSource → CepApi`. Retrofit, OkHttp, Moshi, DTOs e configuração de rede ficam em `core:data`; a apresentação recebe somente modelos de domínio. `CepLookupError` identifica as falhas; o mapper de dados traduz HTTP/JSON/conexão e preserva a causa. As mensagens ficam na apresentação, e cancelamento é propagado.

As composables `Route` obtêm ViewModels pelo Hilt, coletam estados com o ciclo de vida e conectam ações. As `Screen` recebem estado e callbacks. Na Atividade, `ActivityScreen` envia `ActivityAction` e recebe `ActivityUiState`; `ActivityRoute` encaminha cada ação ao responsável. Os comandos de exercício usam `WorkoutAction`, processado pelo `WorkoutViewModel`, que expõe `WorkoutUiState`. Permissões e configurações do Android ficam nos adaptadores de apresentação. O estado da sessão pertence ao gerenciador, inclusive quando a tela está fechada.

`WorkoutSessionManager` contém transições, relógio ativo, filtros GPS e contagem de passos. Usa contratos de repositório, relógio e gerador de ID e é testado sem Android. O serviço recebe o gerenciador com escopo de serviço pelo Hilt, serializa comandos tipados e adapta callbacks de sensores. Não recebe repositórios, DAOs ou entidades Room.

`QuestPoint` valida título e coordenadas; `MissionRules` valida metas e calcula recompensas no domínio. Os repositórios executam as regras de missão dentro das transações que também atualizam missões, progresso e inventário. `WorkoutType` contém apenas modalidades; rótulos e formatação de duração ficam em `feature:activity/presentation/formatter`.

O banco continua sendo `borarpg.db`, versão 5, com identidade `ea976d3679146d8b01d46e65885c1c8c`. A organização dos pacotes mudou o FQN para `com.borarpg.data.local.database.GameDatabase` e o diretório de schemas correspondente; essa mudança não modifica tabelas nem requer nova migração. Os pontos são gravados incrementalmente na mesma transação do resumo. O histórico observa resumos; o percurso é consultado apenas para a atividade exibida. Salvar e descartar retornam resultados explícitos e usam transições condicionais.

O perfil de domínio contém medidas numéricas; `ProfileForm` guarda e valida os textos da edição, incluindo vírgula decimal. `QuestKind`, `QuestRarity` e `QuestFilter` representam categorias fechadas; rótulos ficam na apresentação. Mappers aceitam valores legados do Room/DataStore e gravam códigos estáveis, sem mudar o esquema ou apagar dados.

Mais detalhes em [Arquitetura](docs/ARCHITECTURE.md).

`checkArchitecture` verifica dependências, fronteiras de camadas, referências qualificadas, imports e diretórios. Bloqueia acesso a detalhes de dados fora de `core:data` e uso de repositórios na apresentação; `checkArchitectureRules` testa as regras. A verificação também é executada por `check`.

## Validação

```powershell
.\gradlew.bat checkArchitecture :core:domain:test :core:data:testDebugUnitTest :feature:activity:testDebugUnitTest :feature:profile:testDebugUnitTest :app:assembleDebug lintDebug
.\gradlew.bat :app:assembleDebugAndroidTest :core:data:assembleDebugAndroidTest
```

Com um emulador ou aparelho de teste selecionado:

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest :core:data:connectedDebugAndroidTest :core:tracking:connectedDebugAndroidTest
```

Os testes JVM cobrem regras de RPG, invariantes dos objetivos, perfil, sensor, filtros GPS, pausas, tempo e transições da sessão. Os testes de apresentação verificam formatação, formulário, ações, estado da tela, histórico, erros e proteção contra salvamento duplicado.

Os testes instrumentados verificam persistência após reabrir o banco, migrações 2→5 e 4→5, abertura de banco v5 existente após a reorganização dos pacotes, gravação incremental, rollback, exclusão em cascata, resgate único de recompensas e criação dos ViewModels pelo Hilt na Activity real.

Referências: [Hilt e Gradle](https://dagger.dev/hilt/gradle-setup), [ViewModels com Hilt](https://dagger.dev/hilt/view-model.html) e [Hilt em aplicações com vários módulos](https://developer.android.com/training/dependency-injection/hilt-multi-module).
