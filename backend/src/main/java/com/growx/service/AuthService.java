package com.growx.service;

import com.growx.dto.request.LoginRequest;
import com.growx.dto.request.RegisterRequest;
import com.growx.dto.response.AuthResponse;
import com.growx.entity.Farm;
import com.growx.entity.User;
import com.growx.exception.EmailAlreadyExistsException;
import com.growx.exception.InvalidCredentialsException;
import com.growx.mapper.UserMapper;
import com.growx.repository.FarmRepository;
import com.growx.repository.UserRepository;
import com.growx.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles user registration and login for GrowX.
 *
 * On registration:
 * - Validates email uniqueness
 * - Hashes the password with BCrypt
 * - Creates the User record
 * - Creates an initial Farm record using the provided location
 * - Generates and returns a JWT
 *
 * On login:
 * - Verifies credentials
 * - Generates and returns a JWT
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final FarmRepository farmRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserMapper userMapper;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        // Build and save the user
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .build();
        user = userRepository.save(user);

        // Create the farmer's initial farm using the registration location
        Farm farm = Farm.builder()
                .farmName(request.getName() + "'s Farm")
                .location(request.getLocation())
                .owner(user)
                .build();
        farmRepository.save(farm);

        String token = jwtService.generateToken(user);
        return AuthResponse.builder()
                .token(token)
                .user(userMapper.toResponse(user))
                .build();
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        String token = jwtService.generateToken(user);
        return AuthResponse.builder()
                .token(token)
                .user(userMapper.toResponse(user))
                .build();
    }
}
