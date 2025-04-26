package com.taskmanager.service;

import com.taskmanager.dto.UserLoginDTO;
import com.taskmanager.dto.UserRegisterDTO;
import com.taskmanager.entity.User;
import com.taskmanager.repository.UserRepository;
import com.taskmanager.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public User registerUser(UserRegisterDTO dto) {
        // Check if username or email already exists
        if (userRepository.existsByUsername(dto.username())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(dto.email())) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Create new user
        User user = new User(
                dto.username(),
                dto.email(),
                passwordEncoder.encode(dto.password()),
                dto.role()
        );

        // Save user to database
        return userRepository.save(user);
    }

    public String loginUser(UserLoginDTO dto) {
        User user = userRepository.findByUsername(dto.username())
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));

        if (!passwordEncoder.matches(dto.password(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        return jwtUtil.generateToken(user.getUsername(), user.getRole());
    }
}