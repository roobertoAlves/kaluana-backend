package br.edu.ifsp.kaluana.pairing;

import jakarta.validation.constraints.NotNull;

public record LinkPermissionsDTO(
        @NotNull(message = "Informe o perfil da pessoa.") LinkRelationship relationship,
        boolean events,
        boolean checkins,
        boolean usage,
        boolean tasks,
        boolean reports
) {
    public static LinkPermissionsDTO from(ResponsibleLink link) {
        return new LinkPermissionsDTO(
                link.getRelationship(),
                link.isShareEvents(),
                link.isShareCheckIns(),
                link.isShareUsage(),
                link.isShareTasks(),
                link.isShareReports());
    }
}
