package br.edu.ifsp.kaluana.checkin;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckInServiceTest {

    private static final UUID USER_ID = UUID.randomUUID();

    @Mock
    private CheckInRepository checkInRepository;

    @Test
    void deveSubstituirNivelQuandoJaExisteCheckInNoDia() {
        CheckInService service = new CheckInService(checkInRepository);
        LocalDate today = LocalDate.now();
        CheckIn existing = new CheckIn(USER_ID, today);
        existing.record(2);

        when(checkInRepository.findByUserIdAndDay(USER_ID, today)).thenReturn(Optional.of(existing));
        when(checkInRepository.save(any(CheckIn.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CheckInResponseDTO response = service.record(USER_ID, today, new CheckInRequestDTO(4));

        assertThat(response.level()).isEqualTo(4);
        assertThat(response.date()).isEqualTo(today);
    }

    @Test
    void deveRecusarCheckInDeDiaFuturo() {
        CheckInService service = new CheckInService(checkInRepository);

        assertThatThrownBy(() -> service.record(USER_ID, LocalDate.now().plusDays(5), new CheckInRequestDTO(3)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("dia futuro");
    }
}
