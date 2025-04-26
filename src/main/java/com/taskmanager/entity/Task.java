package com.taskmanager.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String title;

    @Column
    private String description;

    @Column(nullable = false)
    private String status; // e.g "TODO", "IN_PROGRESS", "DONE"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAT;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAT;

    public Task(String title, String description,
                String status, User user) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.user = user;
    }

    @PrePersist
    protected void onCreate() {
        createdAT = LocalDateTime.now();
        updatedAT = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAT = LocalDateTime.now();
    }
}
