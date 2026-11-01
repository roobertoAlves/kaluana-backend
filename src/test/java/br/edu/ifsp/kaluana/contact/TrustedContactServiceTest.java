package br.edu.ifsp.kaluana.contact;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrustedContactServiceTest {

    private static final UUID USER_ID = UUID.randomUUID();

    @Mock
    private TrustedContactRepository trustedContactRepository;

    @Test
    void deveCriarContatoComTelefoneValido() {
        TrustedContactService service = new TrustedContactService(trustedContactRepository);
        when(trustedContactRepository.save(any(TrustedContact.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TrustedContactResponseDTO response = service.create(USER_ID,
                new TrustedContactRequestDTO(" Ana ", ContactMethod.WHATSAPP, "(11) 98888-7777"));

        assertThat(response.name()).isEqualTo("Ana");
        assertThat(response.method()).isEqualTo(ContactMethod.WHATSAPP);
    }

    @Test
    void deveRecusarTelefoneSemDdd() {
        TrustedContactService service = new TrustedContactService(trustedContactRepository);

        assertThatThrownBy(() -> service.create(USER_ID, new TrustedContactRequestDTO("Ana", ContactMethod.LIGACAO, "98888-7777")))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("DDD");
    }

    @Test
    void deveRecusarEmailInvalido() {
        TrustedContactService service = new TrustedContactService(trustedContactRepository);

        assertThatThrownBy(() -> service.create(USER_ID, new TrustedContactRequestDTO("Ana", ContactMethod.EMAIL, "ana@")))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("E-mail");
    }

    @Test
    void naoDeveRemoverContatoDeOutroUsuario() {
        TrustedContactService service = new TrustedContactService(trustedContactRepository);
        UUID contactId = UUID.randomUUID();
        when(trustedContactRepository.findByIdAndUserId(contactId, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(USER_ID, contactId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("não encontrado");
    }
}
