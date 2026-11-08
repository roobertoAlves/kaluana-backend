# Kaluanã - especificação de produto (Product Owner)

Documento de produto do Kaluanã: visão, atores, requisitos com regras e
critérios de aceite, requisitos não funcionais, definição de pronto e backlog.
As regras foram conferidas com o código dos repositórios `kaluana-backend` e
`kaluana-frontend`. A especificação acadêmica completa está no relatório técnico
do TCC (capítulo de método e apêndices).

## 1. Visão e escopo

O Kaluanã é um aplicativo móvel de apoio à organização e à autorregulação de
pessoas com TDAH. Ele ajuda a registrar o dia, lembrar compromissos, dividir
tarefas em passos pequenos, manter o foco e planejar a rotina, e permite
compartilhar, com consentimento, um relatório informativo com um familiar ou
profissional.

Limites do produto:

- O escopo é o TDAH.
- O aplicativo é complementar e não faz diagnóstico, prognóstico nem
  recomendação terapêutica (diretriz NICE, 2018).
- Quem está vinculado vê apenas o que o usuário principal liberou para essa
  pessoa (princípios do ECA e da LGPD).
- A inteligência artificial é opcional, vem desligada e o sistema funciona por
  completo sem ela.
- Fora do escopo: monitoramento por geolocalização, Comunicação Aumentativa e
  Alternativa, sessões de foco acompanhadas por vídeo e bloqueio de outros
  aplicativos.

## 2. Atores

| Ator | Descrição |
| --- | --- |
| Usuário principal (`PRIMARY`) | Pessoa com TDAH que usa todas as funções e decide o que compartilhar. |
| Responsável ou profissional (`RESPONSIBLE`) | Familiar ou profissional de saúde que cria a conta com o código de emparelhamento e vê apenas o que foi liberado para ele. |
| Assistente de IA (opcional) | Serviço compatível com a API de chat da OpenAI (por exemplo, um modelo local no Ollama) que sugere ordem de tarefas e microetapas. |

## 3. Requisitos funcionais

Formato de cada requisito: história de usuário, regras de negócio, critérios de
aceite e rastreabilidade no código.

### RF1 - Registrar evento

- **História:** como usuário, quero registrar o que aconteceu (social,
  impulsividade, urgência ou sobrecarga), para enxergar padrões e ter contexto
  ao conversar com quem me acompanha.
- **Regras:** categoria obrigatória entre `SOCIAL`, `IMPULSIVIDADE`, `URGENCIA`
  e `SOBRECARGA`; nota opcional de até 500 caracteres; marcação de urgência
  opcional; data informada ou, se vazia ou no futuro, a do servidor.
- **Critérios de aceite:**
  - Dado um usuário logado, quando ele registra um evento `SOBRECARGA` com nota,
    então o evento aparece no histórico com data e hora.
  - Dado um evento de outra pessoa, quando o usuário tenta apagá-lo, então a API
    responde 404.
- **Rastreabilidade:** `event/LifeEvent`, `LifeEventController`; migração V3;
  `EventLogScreen`.

### RF2 - Configurar lembrete

- **História:** como usuário, quero lembretes com antecedência e insistência,
  para não perder compromissos.
- **Regras:** horário escolhido no relógio do aparelho; antecedência de 0 a
  1440 minutos; frequência `ONCE`, `DAILY` ou `WEEKLY`; insistência `LOW` (avisa
  1 vez), `MEDIUM` (repete 2 vezes, +5 e +10 min) ou `HIGH` (repete 4 vezes, +2,
  +4, +6 e +8 min); a notificação é local e dispara com o aplicativo fechado.
- **Critérios de aceite:**
  - Dado um lembrete "Tomar remédio" às 08:00 com antecedência de 15 minutos,
    quando ele é salvo, então o primeiro aviso fica agendado para 07:45.
  - Dada a lista de lembretes, então os ativos aparecem em ordem do próximo aviso
    e os pausados à parte.
- **Rastreabilidade:** `reminder/*`; V4; `RemindersScreen`, `ReminderFormSheet`,
  `features/reminders/reminderModel`.

### RF3 - Preencher check-in diário

- **História:** como usuário, quero registrar como está meu dia em uma escala de
  1 a 5, para que o plano do dia se ajuste.
- **Regras:** nível de 1 a 5; um registro por dia (um novo no mesmo dia
  substitui o anterior); sem pontuação nem interpretação.
- **Critérios de aceite:**
  - Dado um check-in nível 2 hoje, quando o plano do dia é montado, então as
    tarefas mais curtas vêm primeiro entre as de mesma urgência.
  - Dado um nível 9, então a API responde 400.
- **Rastreabilidade:** `checkin/*`; V5; `CheckInScreen`; `plan/DayPlanner.order`.

### RF4 - Cadastrar contato de confiança

- **História:** como usuário, quero ter à mão as pessoas que me ajudam, com a
  forma de contato preferida.
- **Regras:** forma `LIGACAO`, `MENSAGEM`, `WHATSAPP` ou `EMAIL`; telefone com
  DDD ou e-mail válido conforme a forma; contato de até 255 caracteres.
- **Critérios de aceite:**
  - Dado um contato por WhatsApp sem DDD, então a API responde 400 com "Informe
    o telefone com DDD".
- **Rastreabilidade:** `contact/*`; V6; `ContactsScreen`.

### RF5 - Gerenciar tarefas e microetapas

- **História:** como usuário, quero dividir tarefas em passos pequenos, para
  começar sem travar.
- **Regras:** título de até 120 caracteres; prioridade `LOW`, `MEDIUM` ou `HIGH`;
  duração opcional de 5 a 480 minutos; data opcional; até 12 microetapas de até
  120 caracteres; concluir ou apagar pede confirmação, reabrir é direto.
- **Critérios de aceite:**
  - Dada uma tarefa com 4 microetapas, quando 2 são marcadas, então o progresso
    mostra 50% e a tarefa continua pendente até ser concluída.
  - Dada a tela de tarefas, então a lista aparece agrupada em atrasadas, hoje,
    próximas, sem data e concluídas.
- **Rastreabilidade:** `task/*`; V7; `TasksScreen`, `TaskFormSheet`,
  `features/tasks/taskModel`.

### RF6 - Emparelhar conta

- **História:** como usuário, quero gerar um código para que alguém da minha
  confiança se vincule à minha conta sem eu passar minha senha.
- **Regras:** código de 6 caracteres sem letras e números parecidos (sem O, 0, I
  e 1); validade de 15 minutos; uso único; qualquer lado pode desfazer o
  vínculo.
- **Critérios de aceite:**
  - Dado um código usado ou vencido, quando o responsável tenta se cadastrar,
    então a API responde 410.
- **Rastreabilidade:** `pairing/*`; V2; `SettingsScreen`, `ResponsibleRegisterScreen`.

### RF7 - Gerar relatório de acompanhamento

- **História:** como profissional vinculado, quero exportar em PDF o que a pessoa
  me autorizou a ver, para usar no acompanhamento.
- **Regras:** período de 1 a 365 dias; só seções liberadas para aquele vínculo;
  identificação de para quem foi gerado; aviso de que não é diagnóstico; o PDF é
  gerado no aparelho; o usuário vê uma prévia "ver como" cada pessoa.
- **Critérios de aceite:**
  - Dado um vínculo sem "Relatórios e PDF", então a API responde 403.
  - Dado um vínculo com apenas check-ins e tarefas liberados, então o relatório
    não contém eventos nem indicadores de uso.
- **Rastreabilidade:** `report/*`; `ReportScreen`, `features/reports/reportHtml`,
  `services/reportPdf`.

### RF8 - Definir o que cada pessoa pode ver

- **História:** como usuário, quero decidir, para cada pessoa vinculada, o que ela
  pode ver.
- **Regras:** perfil `FAMILY`, `HEALTH_PROFESSIONAL` ou `OTHER`; permissões de
  eventos, check-ins, uso, tarefas e relatórios; tudo começa desligado; as
  preferências gerais valem como padrão para novos vínculos; só o usuário
  principal edita.
- **Critérios de aceite:**
  - Dado um responsável, quando ele tenta mudar permissões, então a API responde
    403.
  - Dada uma alteração de permissão, então ela vale no próximo relatório daquela
    pessoa e não muda as das demais.
- **Rastreabilidade:** `pairing/ResponsibleLink`, `access/*`; V10 e V13;
  `LinkPermissionsCard`.

### RF9 - Conduzir sessão de foco

- **História:** como usuário, quero uma sessão de foco com etapas e som opcional
  que não me interrompa.
- **Regras:** 1 a 600 minutos escolhidos no relógio; até 4 etapas; pausa,
  retomada e +1 minuto; som opcional com prévia antes de começar; silenciar vale
  para a sessão inteira; registra só início, fim e forma de encerramento.
- **Critérios de aceite:**
  - Dada uma sessão em andamento, quando o aplicativo é fechado e reaberto depois
    do prazo, então a sessão aparece concluída.
- **Rastreabilidade:** `timer/*`; V8; `TimerPlanScreen`, `TimerSoundScreen`,
  `TimerScreen`, `features/timer/timerSession`.

### RF10 - Planejar o dia

- **História:** como usuário, quero ver as tarefas pendentes distribuídas no dia,
  para saber o que fazer agora.
- **Regras:** ordem por regras (prazo, prioridade, duração e check-in); data e
  janela de horário escolhidas; pausas entre blocos; o usuário aceita, reordena
  ou descarta.
- **Critérios de aceite:**
  - Dada uma janela que termina às 12:00, então tarefas que não cabem vão para
    "não couberam", sem serem apagadas.
- **Rastreabilidade:** `plan/DayPlanner`, `DayPlanService`; V11; `DayPlanScreen`.

### RF11 - Pedir sugestão ao assistente de IA

- **História:** como usuário, quero, se eu quiser, uma sugestão de ordem ou de
  microetapas.
- **Regras:** desligado por padrão; recebe só títulos e durações; até 20 pedidos
  por hora por pessoa; resposta validada; em falha, vale o plano por regras.
- **Critérios de aceite:**
  - Dado o assistente desligado, então `GET /api/assistant/status` informa
    desligado e o plano usa as regras.
- **Rastreabilidade:** `assistant/*`.

### RF12 - Personalizar perfil

- **Regras:** nome de exibição de até 80 caracteres, apelido de até 30 e cor do
  avatar no formato `#RRGGBB`.
- **Critérios de aceite:** dada uma cor inválida, então a API responde 400.
- **Rastreabilidade:** `user/*`; V9; `EditProfileScreen`.

### RF13 - Seguir o guia de autorregulação (planejado para a R6)

- **História:** como usuário, em um momento difícil, quero ser guiado por poucos
  passos simples, escolhendo entre opções fixas.
- **Regras:** máquina de estados finita; 2 ou 3 opções fixas por etapa, sem
  texto livre; estados Em andamento, Interrompida, Concluída e Abandonada; o
  servidor guarda só o resumo da sessão; conteúdo revisado pela equipe e pelo
  orientador.
- **Critérios de aceite:**
  - Dada a mesma sequência de escolhas, então o guia sempre chega à mesma etapa
    (testável sem interface).
  - Dado o prazo de uma etapa vencido, então o guia segue pela opção padrão.

### RF14 - Replanejar o dia

- **Regras:** detecta atraso, conclusão antecipada, tarefa apagada ou nova;
  mantém a ordem aceita e o bloco em andamento; lista o que mudou; pode ser
  desligado, e então apenas avisa.
- **Critérios de aceite:** dada uma tarefa atrasada às 10:30, então os blocos
  seguintes são reposicionados a partir de agora e a lista de mudanças mostra o
  novo horário.
- **Rastreabilidade:** `plan/DayReplanner`; `features/plan/replanner`.

### RF15 - Criar tarefa por frase

- **Regras:** reconhece data ("amanhã", dia da semana), duração ("30min"),
  prioridade ("!alta") e microetapas separadas por ponto e vírgula; mostra o que
  entendeu antes de salvar; cada interpretação pode ser desfeita.
- **Critérios de aceite:** dada a frase "estudar cálculo amanhã 30min !alta",
  então a tarefa "Estudar cálculo" é criada para amanhã, com 30 minutos e
  prioridade alta.
- **Rastreabilidade:** `features/tasks/quickAdd`, `QuickChips`.

### RF16 - Relaxar ou dormir com sons

- **Regras:** paisagens geradas pela equipe (chuva, ondas, lareira, floresta e
  ruídos); Relaxar com respiração guiada; Dormir com volume que diminui até
  parar; 1 a 120 minutos; sugestão pelo período do dia; registra modo, som,
  duração e encerramento.
- **Rastreabilidade:** `ambient/*`; V14; `features/sounds/soundscape`.

### RF17 - Revisar o fim do dia

- **Regras:** mostra o concluído; para cada pendente do dia: amanhã, sem data ou
  concluída; como foi o dia em 3 opções fixas, opcional; até 50 tarefas por
  revisão; uma revisão por dia.
- **Critérios de aceite:** dada uma tarefa levada para amanhã, então a data dela
  passa a ser o dia seguinte.
- **Rastreabilidade:** `review/*`; V15; `DayReviewScreen`.

### RF18 - Criar conta e confirmar e-mail

- **Regras:** a conta nasce inativa; link de uso único, válido por 24 horas e
  guardado só como hash; login antes da confirmação responde 403 (somente com a
  senha certa); reenviar sempre responde 202; contas anteriores à V16 continuam
  ativas.
- **Critérios de aceite:**
  - Dado um cadastro novo, então a resposta não traz tokens e um e-mail é
    enviado.
  - Dado o mesmo link aberto duas vezes, então a segunda resposta é 410.
- **Rastreabilidade:** `auth/EmailVerificationService`, `ConfirmationMailSender`;
  V16; `CheckEmailScreen`; `postman/kaluana_email_confirmation.json`.

## 4. Requisitos não funcionais

| Id | Requisito | Como é verificado |
| --- | --- | --- |
| RNF1 | Regras do plano, do replanejamento, do foco e do guia em funções determinísticas, sem relógio real nem serviço externo. | Testes de `features/*` e da API |
| RNF2 | Lembretes disparam com o aplicativo em segundo plano ou fechado. | Teste em aparelho |
| RNF3 | Quem está vinculado acessa só o autorizado; registros de outra pessoa respondem como inexistentes. | `ReportServiceTest` e coleção do Postman |
| RNF4 | Token de acesso de 30 minutos, renovação com rotação guardada como hash, senha com BCrypt, HTTPS em produção. | `RefreshTokenServiceTest`, `AuthServiceTest` |
| RNF5 | Interface com pouca carga cognitiva: fluxos curtos e poucas opções por tela. | Revisão por critérios no Figma e no aplicativo |
| RNF6 | Nenhuma função diagnóstica, pontuação ou interpretação clínica. | Revisão dos textos e do relatório |
| RNF7 | IA opcional, gratuita, substituível, com dados mínimos e resposta validada. | Testes do assistente |
| RNF8 | Animações curtas que respeitam a redução de movimento do sistema. | `useReducedMotion` |

## 5. Definição de pronto

Uma história está pronta quando:

1. os critérios de aceite passam;
2. as regras novas têm teste automatizado (`npm test` ou `mvn test`);
3. as rotas novas estão na coleção do Postman e passam no Newman em um banco novo;
4. `npx tsc --noEmit` e `npm run lint` não acusam erros;
5. mudanças no banco estão em uma migração nova (nunca editando uma aplicada);
6. a tela foi conferida no aparelho;
7. o relatório técnico foi atualizado quando a mudança afeta requisitos,
   diagramas ou resultados.

## 6. Backlog priorizado

| Prioridade | Item | Critério de aceite principal |
| --- | --- | --- |
| 1 | Teste de ponta a ponta em aparelho Android | Todos os fluxos de RF1 a RF18 executados em aparelho, sem erro. |
| 2 | Auditoria de acesso (R5) | Cada relatório visto ou exportado por um vínculo fica registrado, e o usuário vê "quem viu meus dados" com data e pessoa. |
| 3 | Guia de autorregulação (RF13, R6) | Máquina de estados testada sem interface, telas do guia e registro do resumo da sessão. |
| 4 | IA no guia | O assistente pode apenas reordenar as opções fixas de uma etapa; resposta fora das opções é descartada. |
| 5 | IA na frase da tarefa (RF15) | Quando as regras não reconhecem a frase, o assistente devolve campos validados, mostrados antes de salvar. |
| 6 | Verificação em iOS | Depende de um computador com macOS. |
