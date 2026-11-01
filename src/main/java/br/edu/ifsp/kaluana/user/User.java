package br.edu.ifsp.kaluana.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 255, unique = true)
    private String email;

    @Column(nullable = false, length = 255, name = "hash_password" )
    private String hashPassword;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Column(name = "support_type", length = 20)
    private String typeSupport;

    @Column(length = 30)
    private String nickname;

    @Column(name = "avatar_color", length = 7)
    private String avatarColor;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected User() {

    }
    public User(String name, String email, String hashPassword, UserRole role) {
        this.name = name;
        this.email = email;
        this.hashPassword = hashPassword;
        this.role = role;

    }

    @PrePersist
    void onCreate() {
       OffsetDateTime now = OffsetDateTime.now();
       this.createdAt = now;
       this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    public UUID getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getEmail() {
        return email;
    }
    public String getHashPassword() {
        return hashPassword;
    }
    public UserRole getRole() {
        return role;
    }
    public String getTypeSupport() {
        return typeSupport;
    }
    public void setTypeSupport(String typeSupport) {
        this.typeSupport = typeSupport;
    }
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
    public String getNickname() {
        return nickname;
    }
    public String getAvatarColor() {
        return avatarColor;
    }

    public void updateProfile(String name, String nickname, String avatarColor) {
        this.name = name;
        this.nickname = nickname;
        this.avatarColor = avatarColor;
    }
}
