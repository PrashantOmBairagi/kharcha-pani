package com.prashant.kharchapaniapplication.auth;

import com.prashant.kharchapaniapplication.exception.ResourceNotFoundException;
import com.prashant.kharchapaniapplication.exception.UnauthorizedException;
import com.prashant.kharchapaniapplication.security.JwtService;
import com.prashant.kharchapaniapplication.user.User;
import com.prashant.kharchapaniapplication.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public ResponseEntity<?> login(AuthRequest request) {

        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No account exists with this email. Try signing up."));

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        )) {
            throw new ResourceNotFoundException("Invalid credentials");
        }

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return ResponseEntity.ok(new AuthResponse(accessToken,refreshToken,"Login Successful",user.isProfileComplete())
        );
    }

    public ResponseEntity<?> register(AuthRequest request){
        if(userRepository.existsByEmail(request.getEmail())){
            Map<String,String> error = new HashMap<>();
            error.put("message", "Email already exists! Try Login.");
            return ResponseEntity
                    .badRequest()
                    .body(error);
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setProfileComplete(false);

        userRepository.save(user);

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        return ResponseEntity.ok().body(new AuthResponse(accessToken,refreshToken,"Registration Successful", user.isProfileComplete()));
    }
    public User getCurrentUser() {
        return (User) SecurityContextHolder
                            .getContext()
                            .getAuthentication()
                            .getPrincipal();
    }
    public String refreshTokens(String refreshToken) {
        UUID userId = jwtService.extractUserId(refreshToken);
        User user = userRepository
                .findById(userId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("User Not Found or Token is broken")
                );
        if(jwtService.validateRefreshToken(refreshToken)){
            return jwtService.generateAccessToken(user);
        }
        else {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }
    }
}
