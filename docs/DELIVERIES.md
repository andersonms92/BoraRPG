# Histórico de entregas

O histórico inicial foi organizado retrospectivamente a partir do protótipo existente. Os commits têm as datas reais da organização, sem simular datas anteriores. As etapas registram a evolução; o comportamento atual está no [README](../README.md).

1. Estrutura Android, Kotlin, Compose e Gradle Wrapper.
2. Regras de combate, experiência, níveis e recompensas.
3. Jornada, inventário e persistência com Room e MVVM.
4. Modelo, validação e armazenamento do perfil pessoal.
5. Interface da jornada, mochila e edição do perfil.
6. Mapa interativo, GPS e objetivos pessoais.
7. Consulta de CEP e alternância entre localização manual e GPS.
8. Documentação das funcionalidades e limitações do protótipo.
9. Separação de telas, componentes, integração nativa do mapa e dependências.
10. Formatação Kotlin consistente e remoção de imports e comentários dispensáveis.
11. Remoção dos objetivos e da posição de demonstração do mapa.
12. Contagem real de passos durante atividades, serviço em primeiro plano e migração do progresso de teste.
13. Geração aleatória de objetivos em praças, parques e áreas de lazer reais do OpenStreetMap.
14. Integração ao CEP, novos sorteios, enquadramento dos pontos no mapa e salvamento dos objetivos.
15. Destaque permanente do botão Novos objetivos no Território.
16. Cronômetro da atividade na Jornada e na notificação, com duração final salva localmente.
17. Contratos de domínio e implementações ligadas por Hilt, removendo dependências das features em infraestrutura.
18. Gerenciador de sessão em Kotlin puro e fluxo unidirecional de ações e estado na Atividade.
19. Percursos normalizados, migração Room 4→5, gravação incremental e carregamento sob demanda.
20. Perfil com medidas numéricas e testes de transição, apresentação e persistência.
21. Organização interna de todos os módulos: modelos, contratos e regras no domínio; banco, DAOs, entidades, migrações, preferências, rede, mapeadores e repositórios em data; adaptadores Android separados em tracking.
22. Separação entre Routes e Screens e componentes de apresentação por feature. Na Atividade, `ActivityAction` e `ActivityUiState` organizam a tela; `WorkoutAction` e `WorkoutUiState` organizam o exercício. Formatação de duração e rótulos das modalidades ficam na apresentação.
23. Invariantes de `QuestPoint` e regras de missão no domínio, mantendo as atualizações de progresso e recompensas nas transações de data. Design system organizado em tema, componentes e ilustração; navegação do app com destinos tipados.
24. Verificação `checkArchitecture` para dependências, imports e diretórios dos pacotes, com testes e documentação acompanhando a nova estrutura.

25. Painel da Atividade recolhível em botão flutuante, com expansão animada e indicadores de sessão; permissões e diálogos permanecem ativos ao recolher.

26. Redesenho visual geral com tema azul-escuro, coral e rosa, navegação compacta, cartões de RPG e perfil, métricas adaptáveis, controles flutuantes e mapa com percurso em gradiente. Regras, persistência, missões, recompensas e controles de exercício preservados.

27. Estimativa de kcal e médias atualizada para atividades sem peso original, com identificação do peso atual e acesso ao perfil. Histórico acessível na barra do mapa, com resumos e seleção de percurso.
28. Progressão de XP mais lenta e crescente, preservando os valores acumulados, e indicação explícita do número do guardião e das vitórias.
29. Notificações de novas derrotas emitidas após a transação de progresso, com bônus por vitória, agrupamento de lotes e respeito às permissões do Android.
30. Guardiões com 5.000 de vida e migração Room 5→6, preservando os passos, o dano causado, as vitórias e as recompensas existentes.

As primeiras etapas usavam demonstrações e busca de locais externos. A versão atual registra passos reais e gera encontros virtuais ao redor do CEP, sem Overpass. O painel do exercício e o histórico ficam na aba Atividade.

O banco `borarpg.db` está na versão 6. A migração 5→6 adapta a vida restante do guardião à nova dificuldade sem modificar tabelas; os schemas históricos e as migrações anteriores foram preservados. O FQN de `GameDatabase` e o diretório dos schemas continuam sendo `com.borarpg.data.local.database.GameDatabase`.
