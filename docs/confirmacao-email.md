# Confirmação de e-mail no cadastro

Ao se cadastrar, a conta nasce **inativa**. A API envia um e-mail com um link
(válido por 24 h, uso único); ao clicar, a conta é ativada e o login passa a
funcionar. Antes disso, o login com a senha certa responde `403` pedindo a
confirmação. Contas criadas antes desta mudança continuam ativas.

| Rota | O que faz |
| --- | --- |
| `POST /api/auth/register/*` | Cria a conta pendente e envia o link (resposta sem tokens, `user.emailVerified = false`) |
| `GET /api/auth/confirm-email?token=...` | Link do e-mail: ativa a conta e mostra uma página "volte ao app" |
| `POST /api/auth/resend-confirmation` | Envia um link novo (sempre `202`, não revela se o e-mail existe) |

## Desenvolvimento: Mailpit

Não precisa de conta de e-mail. O Mailpit recebe tudo e mostra numa caixa de
entrada local; nada sai para a internet.

```bash
docker compose -f docker-compose.yml -f docker-compose.mail.yml up -d
```

- Caixa de entrada: http://localhost:8025
- Para testar no celular, o link precisa apontar para o IP do computador:
  `PUBLIC_API_URL=http://192.168.x.x:8080`.

Teste automatizado do fluxo: `postman/kaluana_email_confirmation.json`
(variáveis `baseUrl` e `mailpitUrl`). A collection principal roda com
`EMAIL_CONFIRMATION_REQUIRED=false`, porque cadastra e já entra.

## Depois: Gmail

1. Criar uma conta só para o app (ex.: `kaluana.app@gmail.com`).
2. Ativar a verificação em duas etapas e gerar uma **senha de app**
   (Conta Google › Segurança › Senhas de app).
3. Subir a API **sem** o `docker-compose.mail.yml`, com:

```env
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=kaluana.app@gmail.com
MAIL_PASSWORD=<senha de app, nunca no Git>
MAIL_SMTP_AUTH=true
MAIL_STARTTLS=true
MAIL_FROM=Kaluanã <kaluana.app@gmail.com>
PUBLIC_API_URL=https://endereco-publico-da-api
```

O Gmail limita o volume diário de envios; para produção com muitos usuários,
um serviço transacional (Brevo, Amazon SES, Resend) usa as mesmas variáveis.
