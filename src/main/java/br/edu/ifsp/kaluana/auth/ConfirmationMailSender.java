package br.edu.ifsp.kaluana.auth;

import br.edu.ifsp.kaluana.auth.EmailVerificationService.EmailConfirmationRequested;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.util.HtmlUtils;

import java.nio.charset.StandardCharsets;

/**
 * Envia o e-mail de confirmação depois que o cadastro foi gravado: se o
 * cadastro falhar, nada é enviado. Se o SMTP falhar, o cadastro continua
 * valendo e a pessoa pede outro link pelo app ("Reenviar e-mail").
 */
@Component
public class ConfirmationMailSender {

    private static final Logger log = LoggerFactory.getLogger(ConfirmationMailSender.class);

    private final JavaMailSender mailSender;
    private final MailProperties properties;

    public ConfirmationMailSender(JavaMailSender mailSender, MailProperties properties) {
        this.mailSender = mailSender;
        this.properties = properties;
    }

    @TransactionalEventListener(fallbackExecution = true)
    public void onConfirmationRequested(EmailConfirmationRequested request) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setFrom(properties.from());
            helper.setTo(request.email());
            helper.setSubject("Confirme seu e-mail no Kaluanã");
            helper.setText(plainText(request), html(request));
            mailSender.send(message);
        } catch (MailException | MessagingException e) {
            log.warn("Não foi possível enviar o e-mail de confirmação: {}", e.getMessage());
        }
    }

    static String plainText(EmailConfirmationRequested r) {
        return "Olá, " + r.name() + ".\n\n"
                + "Para ativar sua conta no Kaluanã, abra o link abaixo:\n"
                + r.link() + "\n\n"
                + "O link vale por 24 horas. Se não foi você que criou a conta, ignore este e-mail.";
    }

    static String html(EmailConfirmationRequested r) {
        String name = HtmlUtils.htmlEscape(r.name());
        String link = HtmlUtils.htmlEscape(r.link());
        return """
                <div style="font-family:Arial,sans-serif;color:#1F3556;max-width:480px">
                  <p style="font-size:11px;letter-spacing:2px;color:#C95D2B;font-weight:bold">KALUANÃ</p>
                  <h1 style="font-size:22px">Olá, %s.</h1>
                  <p>Falta só um passo para ativar sua conta.</p>
                  <p><a href="%s" style="display:inline-block;background:#E9782D;color:#fff;padding:14px 20px;text-decoration:none;font-weight:bold">CONFIRMAR MEU E-MAIL</a></p>
                  <p style="font-size:13px">O link vale por 24 horas. Se não foi você que criou a conta, ignore este e-mail.</p>
                </div>
                """.formatted(name, link);
    }
}
