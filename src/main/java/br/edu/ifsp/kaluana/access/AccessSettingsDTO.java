package br.edu.ifsp.kaluana.access;

/** Mesmo formato do AccessSettings do app; serve de entrada e de saída. */
public record AccessSettingsDTO(
        boolean events,
        boolean checkins,
        boolean usage,
        boolean reports
) {
    public static AccessSettingsDTO from(AccessSettings settings) {
        return new AccessSettingsDTO(
                settings.isShareEvents(),
                settings.isShareCheckIns(),
                settings.isShareUsage(),
                settings.isShareReports());
    }
}
