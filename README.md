# BoraRPG

App Android de aventura por movimento, em Kotlin, Jetpack Compose e MVVM.

## Executar

Abra no Android Studio e execute o módulo app. Requer SDK 37 e JDK compatível com o Gradle configurado.

```powershell
.\gradlew.bat testDebugUnitTest assembleDebug lintDebug
```

## Atividades e RPG

A jornada registra caminhada e corrida pelo contador de passos do aparelho. O usuário inicia e encerra a atividade; um serviço em primeiro plano mantém a contagem com a tela bloqueada e oferece a ação de encerrar na notificação.

A primeira leitura define a referência da sessão. Passos anteriores e passos feitos entre sessões não são contabilizados. Cada passo registrado causa dano; cada dez passos concedem um XP. Uma vitória concede 50 XP adicionais, 25 de ouro e um fragmento. Dano excedente continua no próximo encontro, e lotes diferentes do sensor produzem as mesmas recompensas.

Aparelhos sem contador compatível mostram indisponibilidade. Não há gerador artificial de passos, estimativa de distância por passos, gravação de trajetos ou registro de ciclismo. A atividade não reinicia automaticamente após o processo ser encerrado pelo sistema.

O banco Room preserva o progresso real. A migração da versão 1 para a 2 remove somente o progresso da jornada, que era gerado por simulações. Perfil e objetivos pessoais, armazenados em DataStore, são preservados.

## Perfil

Nome, peso, altura e idade são validados e salvos localmente. As medidas ainda não alteram a prescrição de atividade ou as recompensas.

## Território

MapLibre Native e OpenFreeMap renderizam o mapa online. Não há pontos pré-cadastrados nem posição inicial representando o usuário. O mapa é centralizado pelo GPS ou por consulta de CEP à BrasilAPI. As coordenadas por CEP são aproximadas; a consulta pode retornar endereço sem coordenadas.

Segure o mapa para salvar um objetivo pessoal; toque para consultar ou remover. Ao consultar um CEP, o app sorteia até seis objetivos de exploração em locais cadastrados no OpenStreetMap, em um raio de 2 km. O botão Novos objetivos sorteia novamente entre os candidatos disponíveis, usando cache durante a sessão. Os locais gerados podem ser salvos para permanecerem após reiniciar. A consulta exclui acessos explicitamente privados ou restritos, mas os dados do mapa não garantem acesso ou horários atuais. Regiões sem cobertura e falhas de rede têm mensagens próprias. Os objetivos ainda não concedem recompensas de visita.

A localização do mapa é coletada somente com a área aberta e o app em primeiro plano. O contador de passos da atividade é independente do GPS. O provedor de mapas recebe as requisições da região visualizada; a BrasilAPI recebe o CEP consultado, e a Overpass API recebe as coordenadas da região para buscar os locais próximos. Não há envio do perfil para um servidor próprio.

## Organização

- `domain`: modelos, validações e regras puras.
- `data`: Room, DataStore e acesso a fontes externas.
- `tracking`: serviço de contagem de passos.
- `di`: composição das dependências.
- `ui/screens`: telas da jornada, mochila e perfil.
- `ui/map`: território, integração nativa e diálogo de CEP.
- `ui/components` e `ui/theme`: componentes e identidade visual.

## Testes

Os testes cobrem combate, recompensas, validação de perfil e CEP, leitura inicial do sensor, leituras duplicadas, reinício do contador, separação entre sessões e equivalência de lotes. Precisão e consumo de bateria devem ser validados em aparelho físico compatível.

Referências: https://developer.android.com/develop/sensors-and-location/sensors/sensors_motion , https://openfreemap.org/quick_start/ e https://brasilapi.com.br/docs .

## Tempo de atividade
O cronômetro na Jornada começa ao iniciar uma atividade e termina ao encerrá-la, incluindo o período com a tela bloqueada. A notificação também exibe o tempo em andamento. A duração da última atividade encerrada é salva localmente. O contador usa tempo monotônico para não depender de alterações no relógio do aparelho. Uma atividade interrompida pelo encerramento do processo não é retomada automaticamente.
O botão Novos objetivos fica sempre visível acima do painel inferior do Território; sem um CEP selecionado, abre a busca por CEP.
