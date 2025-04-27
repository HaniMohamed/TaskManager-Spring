package com.taskmanager.service;

import com.taskmanager.dto.UserLoginDTO;
import com.taskmanager.dto.UserRegisterDTO;
import com.taskmanager.entity.User;
import com.taskmanager.exception.ApiException;
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
            throw new ApiException("Username already exists", "USERNAME_TAKEN");
        }
        if (userRepository.existsByEmail(dto.email())) {
            throw new ApiException("Email already exists", "EMAIL_TAKEN");
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
                .orElseThrow(() -> new ApiException("Invalid username or password", "INVALID_CREDENTIALS"));

        if (!passwordEncoder.matches(dto.password(), user.getPassword())) {
            throw new ApiException("Invalid username or password", "INVALID_CREDENTIALS");
        }

        return jwtUtil.generateToken(user.getUsername(), user.getRole());
    }
}