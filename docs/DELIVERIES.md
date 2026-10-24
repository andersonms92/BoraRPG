# Histórico de entregas

O histórico inicial foi organizado retrospectivamente a partir do protótipo existente. Os commits têm as datas reais da organização, sem simular datas anteriores. Cada uma das sete etapas de implementação foi compilada e validada com os testes disponíveis naquele ponto.

1. Estrutura Android, Kotlin, Compose e Gradle Wrapper.
2. Regras de combate, experiência, níveis e recompensas.
3. Jornada, inventário e persistência com Room e MVVM.
4. Modelo, validação e armazenamento do perfil pessoal.
5. Interface da jornada, mochila e edição do perfil.
6. Mapa interativo, GPS e objetivos pessoais.
7. Consulta de CEP e alternância entre localização manual e GPS.
8. Documentação das funcionalidades e limitações do protótipo.

Os textos de commit descrevem o conteúdo entregue. Mapas e consulta de CEP precisam de internet; o combate ainda usa passos simulados.
9. Separação de telas, componentes, integração nativa do mapa e dependências.
10. Formatação Kotlin consistente e remoção de imports e comentários dispensáveis.

11. Remoção dos objetivos e da posição de demonstração do mapa.
12. Contagem real de passos durante atividades, serviço em primeiro plano e migração do progresso de teste.

13. Geração aleatória de objetivos em praças, parques e áreas de lazer reais do OpenStreetMap.
14. Integração ao CEP, novos sorteios, enquadramento dos pontos no mapa e salvamento dos objetivos.

Validação da entrega 14: compilação, 17 testes e lint passaram. As consultas reais aos servidores Overpass tiveram timeout neste ambiente; a geração online de ponta a ponta ainda precisa ser confirmada com o serviço disponível.

15. Destaque permanente do botão Novos objetivos no Território.
16. Cronômetro da atividade na Jornada e na notificação, com duração final salva localmente.
