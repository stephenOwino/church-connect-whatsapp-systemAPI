package com.stephenotieno.church_whatsapp_system.churchconnect.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "churches")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Church {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String location;

    private String phone;

    @Column(name = "mpesa_shortcode")
    private String mpesaShortcode;

    @Column(name = "mpesa_passkey", columnDefinition = "TEXT")
    private String mpesaPasskey;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // FIX: Add @JsonManagedReference to prevent circular reference
    @OneToMany(mappedBy = "church", cascade = CascadeType.ALL)
    @JsonManagedReference
    @ToString.Exclude // Also exclude from toString to prevent issues
    private List<Admin> admins;

    @OneToMany(mappedBy = "church", cascade = CascadeType.ALL)
    @JsonManagedReference
    @ToString.Exclude
    private List<Member> members;

    @OneToMany(mappedBy = "church", cascade = CascadeType.ALL)
    @JsonManagedReference
    @ToString.Exclude
    private List<Group> groups;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
