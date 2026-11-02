package br.edu.ifsp.kaluana.ambient;

import br.edu.ifsp.kaluana.timer.TimerSessionStatus;
import br.edu.ifsp.kaluana.timer.TimerSound;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AmbientSessionRequestDTOTest {

    private static final OffsetDateTime START = OffsetDateTime.parse("2026-11-02T22:00:00-03:00");

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void sessaoDeDormirComSomEhValida() {
        assertThat(validate(TimerSound.WAVES, 30, START.plusMinutes(30))).isEmpty();
    }

    @Test
    void relaxarEDormirPrecisamDeUmSom() {
        assertThat(messages(validate(TimerSound.NONE, 10, START.plusMinutes(10)))).contains("Relaxar e Dormir precisam de um som.");
    }

    @Test
    void duracaoEPeriodoPrecisamSerValidos() {
        assertThat(messages(validate(TimerSound.RAIN, 121, START.minusMinutes(1))))
                .contains("A sessão pode ter até 120 minutos.", "O fim da sessão precisa ser depois do início.");
    }

    private Set<ConstraintViolation<AmbientSessionRequestDTO>> validate(TimerSound sound, int minutes, OffsetDateTime finishedAt) {
        return validator.validate(new AmbientSessionRequestDTO(AmbientMode.SLEEP, sound, minutes, TimerSessionStatus.DONE, START, finishedAt));
    }

    private static Set<String> messages(Set<ConstraintViolation<AmbientSessionRequestDTO>> violations) {
        return violations.stream().map(ConstraintViolation::getMessage).collect(java.util.stream.Collectors.toSet());
    }
}
