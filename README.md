# BoraRPG
App Android de aventura por movimento. Kotlin, Jetpack Compose, MVVM, Room e DataStore.

## Executar
Abra esta pasta no Android Studio e execute o módulo app. Requer JDK 17+ e SDK 37.
Windows: .\gradlew.bat testDebugUnitTest assembleDebug

## Interface
Jornada com arte vetorial própria, cores carvão/lima/coral, tipografia editorial, progressos animados e navegação Jornada / Território / Mochila / Você.

## Perfil
Nome, peso (kg), altura (cm) e idade, com validação, feedback de erro e persistência DataStore local. Aceita vírgula decimal. Os dados não alteram treinos ou dano nesta etapa. Não há envio do perfil ao servidor.

## Mapa e localização
MapLibre Native OpenGL 13.6.1 + OpenFreeMap (mapas online, sem chave). Requer conexão para carregar mapas. O provedor recebe as requisições dos mapas da região visualizada; veja sua política em https://openfreemap.org/privacy/ . Não há backend próprio.
- Solicita localização precisa/aproximada somente ao acionar o GPS.
- Atualiza o ponto azul e acompanha a câmera; arrastar interrompe o acompanhamento, e o botão de localização o retoma.
- Coleta apenas enquanto a área do mapa está visível e o app está em primeiro plano. Não grava trajetos nem atividades físicas.
- Filtros de combate/exploração e seleção de objetivos com distância em linha reta.
- Três pontos DEMONSTRATIVOS fixos no Ibirapuera, São Paulo. Não são missões reais nem garantem acesso ao lugar; não concedem recompensas.
- Pressione e segure no mapa para salvar um objetivo pessoal. Toque para ver ou remover. Dados ficam em DataStore no aparelho.
- Não há navegação de trajetos, geração automática de missões seguras, catálogo regional, sincronização ou chefe compartilhado.

## Regras do protótipo
Combate ainda usa passos simulados, acessíveis em Laboratório do protótipo. 250 passos causam 250 de dano e 25 XP; vitória concede mais 50 XP, 25 de ouro e um fragmento. 100 XP por nível. Não há coleta real de passos nem de ciclismo. Room preserva progresso e atualização transacional sem migração destrutiva.

## Arquitetura
- domain: modelos e validação de perfil; regras puras de combate.
- data: Room, repository de aventura, DataStore e fluxo de localização com limpeza de listeners.
- ui: ViewModels, estado, telas Compose e integração com MapView/lifecycle.
- MainActivity: composição das dependências.

## Referências de design e implementação
- https://design.google/library/expressive-material-design-google-research
- https://support.komoot.com/hc/en-us/articles/10207999797530-Find-routes-and-inspiration-on-komoot
- https://maplibre.org/maplibre-native/android/examples/getting-started/
- https://openfreemap.org/quick_start/

## Testes
Testes unitários cobrem recompensas únicas, dano parcial, passos inválidos e validação de perfil (vírgula, limites, campos ausentes e idade inteira). Verificações no emulador complementam os testes; não comprovam precisão ou consumo de bateria em aparelho físico.

## Busca por CEP
Território > Buscar CEP consulta BrasilAPI CEP V2 e centraliza o mapa nas coordenadas retornadas. Aceita 8 dígitos ou 00000-000. Requer internet; CEP inexistente, falhas de rede e coordenadas ausentes têm mensagens próprias. A região por CEP pausa o acompanhamento da câmera pelo GPS e serve de referência para as distâncias dos objetivos. O botão de localização retoma o GPS. Não altera os sensores nem concede recompensas. A consulta envia apenas o CEP à BrasilAPI; a seleção permanece durante a sessão do app.
Documentação: https://brasilapi.com.br/docs#tag/CEP-V2
