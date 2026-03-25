package com.superduperdoodle.service;

import com.superduperdoodle.dto.AuthResponse;
import com.superduperdoodle.dto.LoginRequest;
import com.superduperdoodle.dto.RegisterRequest;
import com.superduperdoodle.entity.User;
import com.superduperdoodle.repository.UserRepository;
import com.superduperdoodle.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Username already taken");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already registered");
        }

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        userRepository.save(user);

        String token = jwtService.generateToken(user.getUsername());
        return new AuthResponse(token, user.getUsername(), user.getRole());
    }

    public AuthResponse login(LoginRequest request) {
        String identifier = request.username();

        User user = userRepository.findByUsername(identifier)
            .or(() -> userRepository.findByEmail(identifier))
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(user.getUsername(), request.password())
        );

        String token = jwtService.generateToken(user.getUsername());
        return new AuthResponse(token, user.getUsername(), user.getRole());
    }
}
