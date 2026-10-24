# BoraRPG

App Android de aventura por movimento, em Kotlin, Jetpack Compose e MVVM. Dados locais em Room e DataStore.

## Executar

Abra no Android Studio e execute o módulo app. Requer SDK 37 e JDK compatível com o Gradle configurado.

```powershell
.\gradlew.bat testDebugUnitTest assembleDebug lintDebug
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

Informe um CEP na aba Território para gerar seis encontros virtuais em até 1 km do centro aproximado. Novos objetivos sorteia novamente posições, metas e recompensas de raridade comum, rara ou épica. Os marcadores mostram XP, ouro e itens; não dependem de uma consulta de pontos de interesse externos. Salve um encontro para mantê-lo no catálogo ou aceite uma missão, que permanece ativa após reiniciar.

Aceitar uma missão inicia uma meta de passos futuros. Os passos reais registrados durante atividades avançam a meta. Ao concluí-la, resgate XP, ouro e um item na mochila; o banco impede resgate duplicado. Há uma missão ativa por vez. Os encontros são virtuais: não é necessário entrar no terreno indicado pelo marcador.

Cada passo registrado também causa dano no combate da jornada; cada dez passos concedem um XP. Uma vitória concede 50 XP, 25 de ouro e um fragmento. A primeira leitura do sensor estabelece a referência; passos anteriores à atividade ou feitos durante pausas não entram na contagem.

## Território e dados

MapLibre Native e OpenFreeMap renderizam o mapa online. O GPS ou a busca de CEP pela BrasilAPI centralizam a região; coordenadas de CEP são aproximadas e nem todo endereço possui coordenadas. Segure o mapa para salvar um local pessoal.

Perfil, progresso, missões, inventário e percursos ficam no aparelho. Não há API própria ou sincronização entre dispositivos. O provedor de mapas recebe a região visualizada e a BrasilAPI recebe o CEP consultado. Não há mais consultas à Overpass API. A localização de visualização do Território só é coletada enquanto a tela está aberta; durante uma atividade, o serviço mantém a coleta GPS até pausar ou finalizar.

A migração Room 1→2 remove apenas o progresso simulado antigo. As migrações 2→3 e 3→4 acrescentam missões, inventário e atividades, preservando o progresso real.

## Organização e validação

- `domain`: modelos, relógio, distância, calorias e regras puras.
- `data`: Room, DataStore, migrações e acesso a fontes externas.
- `tracking`: serviço GPS e contador de passos.
- `di`: composição das dependências.
- `ui/screens`, `ui/map`, `ui/components`, `ui/theme`: telas, mapas e identidade visual.

Os testes unitários cobrem combate, recompensas, perfil, CEP, geração de objetivos, sensor, pausas, distância e calorias. Os testes instrumentados verificam persistência do percurso após reabrir o banco, recuperação de sessão, migração 2→4 e resgate único de recompensas.
