package br.edu.ifsp.kaluana.access;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * RF8: padrão de compartilhamento do usuário principal. Vale para quem se
 * conectar depois; cada vínculo tem a sua própria autorização (ResponsibleLink).
 */
@Entity
@Table(name = "access_settings")
public class AccessSettings {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "share_events", nullable = false)
    private boolean shareEvents;

    @Column(name = "share_check_ins", nullable = false)
    private boolean shareCheckIns;

    @Column(name = "share_usage", nullable = false)
    private boolean shareUsage;

    @Column(name = "share_reports", nullable = false)
    private boolean shareReports;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected AccessSettings() {
        // exigido pelo JPA
    }

    /** Padrão seguro: nada compartilhado até o usuário ligar. */
    public static AccessSettings nothingSharedFor(UUID userId) {
        AccessSettings settings = new AccessSettings();
        settings.userId = userId;
        return settings;
    }

    @PrePersist
    @PreUpdate
    void touch() {
        this.updatedAt = OffsetDateTime.now();
    }

    public void update(boolean shareEvents, boolean shareCheckIns, boolean shareUsage, boolean shareReports) {
        this.shareEvents = shareEvents;
        this.shareCheckIns = shareCheckIns;
        this.shareUsage = shareUsage;
        this.shareReports = shareReports;
    }

    public UUID getUserId() {
        return userId;
    }

    public boolean isShareEvents() {
        return shareEvents;
    }

    public boolean isShareCheckIns() {
        return shareCheckIns;
    }

    public boolean isShareUsage() {
        return shareUsage;
    }

    public boolean isShareReports() {
        return shareReports;
    }
}
