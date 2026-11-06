package br.edu.ifsp.kaluana.reminder;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ReminderService {

    private final ReminderRepository reminderRepository;

    public ReminderService(ReminderRepository reminderRepository) {
        this.reminderRepository = reminderRepository;
    }

    @Transactional(readOnly = true)
    public List<ReminderResponseDTO> list(UUID userId) {
        return reminderRepository.findByUserIdOrderByTimeAsc(userId).stream()
                .map(ReminderResponseDTO::from)
                .toList();
    }

    @Transactional
    public ReminderResponseDTO create(UUID userId, ReminderRequestDTO request) {
        Reminder reminder = new Reminder(userId);
        applyRequest(reminder, request);
        return ReminderResponseDTO.from(reminderRepository.save(reminder));
    }

    @Transactional
    public ReminderResponseDTO update(UUID userId, UUID reminderId, ReminderRequestDTO request) {
        Reminder reminder = findOwned(userId, reminderId);
        applyRequest(reminder, request);
        return ReminderResponseDTO.from(reminder);
    }

    @Transactional
    public void delete(UUID userId, UUID reminderId) {
        reminderRepository.delete(findOwned(userId, reminderId));
    }

    private void applyRequest(Reminder reminder, ReminderRequestDTO request) {
        reminder.configure(
                request.title().trim(),
                request.time(),
                request.leadMinutes(),
                request.frequency(),
                request.insistence(),
                request.enabledOrDefault());
    }

    private Reminder findOwned(UUID userId, UUID reminderId) {
        return reminderRepository.findByIdAndUserId(reminderId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lembrete não encontrado."));
    }
}
