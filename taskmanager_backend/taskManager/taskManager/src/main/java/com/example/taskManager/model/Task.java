package com.example.taskManager.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // Follow naming convention (lowercase 'i')

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    private boolean completed = false;

    /**
     * Each task belongs to a single user.
     * This prevents users from accessing others' tasks.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore // prevent recursion during serialization
    @ToString.Exclude
    private User user;
}
