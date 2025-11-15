package com.example.taskManager.services;

import com.example.taskManager.exception.InvalidCredentialsException;
import com.example.taskManager.exception.UserAlreadyExistsException;
import com.example.taskManager.exception.UserNotFoundException;
import com.example.taskManager.model.User;
import com.example.taskManager.repository.UserRepository;
import com.example.taskManager.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registers a new user by encoding the password and saving to DB.
     */
    public User register(String username, String password) {

        if (userRepository.existsByUsername(username)) {
            throw new UserAlreadyExistsException("Username already exists");
        }

        // Hash password first
        String hashedPassword = passwordEncoder.encode(password);

        // Create user using the constructor we added (generic-ready)
        User newUser = new User(username, hashedPassword);
        newUser.setLoggedIn(false);

        return userRepository.save(newUser);
    }

    /**
     * Authenticates a user and generates a JWT if successful.
     */
    public String login(String username, String password) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        user.setLoggedIn(true);
        userRepository.save(user);

        return jwtUtil.generateToken(username);
    }

    /**
     * Logs out a user (optional, JWT remains valid until expiration).
     */
    public void logout(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setLoggedIn(false);
        userRepository.save(user);
    }
}
