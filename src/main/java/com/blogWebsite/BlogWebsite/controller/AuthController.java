package com.blogWebsite.BlogWebsite.controller;

import com.blogWebsite.BlogWebsite.dto.AuthResponse;
import com.blogWebsite.BlogWebsite.dto.UserResponse;
import com.blogWebsite.BlogWebsite.entity.User;
import com.blogWebsite.BlogWebsite.repository.UserRepository;
import com.blogWebsite.BlogWebsite.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(AuthenticationManager authenticationManager,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public record SignupRequest(String username, String name, String email,
                                String profilePicBase64, String password,
                                Set<String> roles) {}

    public record LoginRequest(String email, String password) {}
    public record ApiResponse(String message) {}

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest req) {
        if (req.email() == null || req.email().isBlank() || req.password() == null || req.password().isBlank()) {
            return ResponseEntity.badRequest().body(new ApiResponse("Email and password are required"));
        }

        if (userRepository.findByEmail(req.email().trim()).isPresent()) {
            return ResponseEntity.badRequest().body(new ApiResponse("Email already exists"));
        }

        Set<String> roles = (req.roles() != null && !req.roles().isEmpty()) ? req.roles() : Set.of("USER");

        User user = new User();
        user.setUsername(req.username());
        user.setName(req.name());
        user.setEmail(req.email());
        user.setProfilePicBase64(req.profilePicBase64);
        user.setPassword(passwordEncoder.encode(req.password()));
        user.setRoles(roles);

        userRepository.save(user);
        logger.info("User signed up: {}", user.getEmail());
        return ResponseEntity.ok(new ApiResponse("User registered successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.email(), req.password())
            );
        } catch (BadCredentialsException ex) {
            logger.warn("Failed login attempt for email: {}", req.email());
            return ResponseEntity.status(401).body(new ApiResponse("Invalid credentials"));
        }

        User user = userRepository.findByEmail(req.email().trim())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String token = jwtUtil.generateToken(user.getEmail(), user.getRoles());
        logger.info("User logged in: {}", user.getEmail());
        return ResponseEntity.ok(new AuthResponse(token, user.getRoles()));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal org.springframework.security.core.userdetails.User principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(new ApiResponse("Unauthorized"));
        }

        User user = userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return ResponseEntity.ok(new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getName(),
                user.getEmail(),
                user.getRoles(),
                user.getProfilePicBase64()
        ));
    }
}
