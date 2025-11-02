package com.example.taskManager.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "password") // Prevent password leak in logs
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    /**
     * Password is stored in BCrypt-hashed form.
     */
    @Column(nullable = false)
    private String password;

    @Column(name = "is_logged_in")
    private boolean loggedIn = false;

    /**
     * Each user can have multiple tasks.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Task> tasks;

    /**
     * Helper constructor (no ID, default loggedIn=false)
     */
    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.loggedIn = false;
    }
}
