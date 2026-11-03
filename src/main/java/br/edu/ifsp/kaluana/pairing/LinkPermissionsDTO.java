package br.edu.ifsp.kaluana.pairing;

import jakarta.validation.constraints.NotNull;

/** RF8: o que uma pessoa vinculada pode ver. Serve de entrada e de saída. */
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
