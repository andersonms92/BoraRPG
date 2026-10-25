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

As primeiras etapas usavam demonstrações e busca de locais externos. A versão atual registra passos reais e gera encontros virtuais ao redor do CEP, sem Overpass. O painel do exercício e o histórico ficam na aba Atividade.

A reorganização mantém `borarpg.db` na versão 5, com identidade `ea976d3679146d8b01d46e65885c1c8c`. Apenas o FQN de `GameDatabase` e o diretório dos schemas mudaram para `com.borarpg.data.local.database.GameDatabase`; os schemas e as migrações existentes foram preservados.
