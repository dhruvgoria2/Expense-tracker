package com.dhruvgoria.expense_tracker.controller;

import com.dhruvgoria.expense_tracker.config.JwtUtil;
import com.dhruvgoria.expense_tracker.entity.User;
import com.dhruvgoria.expense_tracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public User register(@RequestBody User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Map<String, String> loginRequest) {
        // 1. FIXED: Extract "username" from the Postman JSON body instead of "email"
        String email = loginRequest.get("email");
        String rawPassword = loginRequest.get("password");

        // 2. Fetch the user from the database by their unique username
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 3. Verify the password matches your BCrypt encoder settings
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }

        // 4. Generate and hand back your secure JWT Token string
        String token = jwtUtil.generateToken(user.getEmail());

        java.util.Map<String, String> response = new java.util.HashMap<>();
        response.put("token", token);
        return response;
    }
}