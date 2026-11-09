# Diagramas do Kaluanã

Fontes em PlantUML (`.puml`) e as imagens geradas em `png/`. Todos foram conferidos com o código e com as migrações do banco (`V1` a `V18`).

| Arquivo | O que mostra |
| --- | --- |
| `use-case-diagram.puml` | Casos de uso por área (registro e rotina, tarefas e foco, planejamento, vínculo e relatório), com os atores Usuário, Responsável ou profissional e Assistente de IA (opcional). |
| `class-diagram.puml` | Entidades e métodos reais. As linhas cheias são chaves estrangeiras do banco; as tracejadas são dependências sem chave estrangeira (por exemplo, `PlannedBlock` guarda o id da tarefa dentro do plano). |
| `architecture-diagram.puml` | Aplicativo (Expo, axios), Caddy, API Spring Boot, PostgreSQL, Redis, SMTP e o assistente de IA opcional. |
| `focus-session-state-diagram.puml` | Estados da sessão de foco (RF9). |
| `guide-session-state-diagram.puml` | Estados da sessão do guia de autorregulação (RF13). |
| `replanning-activity-diagram.puml` | Atividades do replanejamento do dia (RF14). |
| `report-export-sequence-diagram.puml` | Sequência da exportação do relatório em PDF (RF7), com a autorização por vínculo e o registro do acesso (RF8). |

## Como gerar as imagens

Com Docker:

```bash
docker run --rm -e PLANTUML_LIMIT_SIZE=16384 -v "$PWD/diagrams:/data" plantuml/plantuml:latest -tpng -charset UTF-8 -o /data/png /data/*.puml
```

Ou pela extensão PlantUML do VS Code (`jebbs.plantuml`), abrindo o arquivo e usando `Alt + D`.
