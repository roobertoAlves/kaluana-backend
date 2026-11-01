package br.edu.ifsp.kaluana.reminder;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalTime;
import java.util.UUID;

public record ReminderResponseDTO(
        UUID id,
        String title,
        @JsonFormat(pattern = "HH:mm")
        LocalTime time,
        int leadMinutes,
        ReminderFrequency frequency,
        ReminderInsistence insistence,
        boolean enabled
) {
    public static ReminderResponseDTO from(Reminder reminder) {
        return new ReminderResponseDTO(
                reminder.getId(),
                reminder.getTitle(),
                reminder.getTime(),
                reminder.getLeadMinutes(),
                reminder.getFrequency(),
                reminder.getInsistence(),
                reminder.isEnabled());
    }
}
