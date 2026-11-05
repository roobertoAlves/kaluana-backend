package br.edu.ifsp.kaluana.auth;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.HtmlUtils;

/**
 * O link do e-mail abre no navegador do celular, então a confirmação devolve
 * uma página simples (não JSON) dizendo para voltar ao app e entrar.
 */
@RestController
@RequestMapping("/api/auth")
public class EmailConfirmationController {

    private final EmailVerificationService verificationService;

    public EmailConfirmationController(EmailVerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @GetMapping(value = "/confirm-email", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> confirm(@RequestParam(defaultValue = "") String token) {
        try {
            String name = verificationService.confirm(token).getName();
            return ResponseEntity.ok(page("E-mail confirmado", "Pronto, " + name + ". Sua conta está ativa: volte ao app e entre."));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(page("Link inválido", EmailVerificationService.INVALID_LINK));
        }
    }

    /** Sempre 202: não revela se o e-mail tem conta nem se já foi confirmado. */
    @PostMapping("/resend-confirmation")
    public ResponseEntity<Void> resend(@Valid @RequestBody ResendRequest request) {
        verificationService.resend(request.email());
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    public record ResendRequest(@NotBlank(message = "Informe o e-mail.") @Email(message = "Informe um e-mail válido.") String email) {
    }

    private static String page(String title, String text) {
        return """
                <!doctype html><html lang="pt-BR"><head><meta charset="utf-8">
                <meta name="viewport" content="width=device-width,initial-scale=1"><title>Kaluanã · %s</title></head>
                <body style="font-family:Arial,sans-serif;background:#FFF9F2;color:#1F3556;padding:32px">
                <p style="font-size:11px;letter-spacing:2px;color:#C95D2B;font-weight:bold">KALUANÃ</p>
                <h1>%s</h1><p style="font-size:17px">%s</p></body></html>
                """.formatted(HtmlUtils.htmlEscape(title), HtmlUtils.htmlEscape(title), HtmlUtils.htmlEscape(text));
    }
}
