package com.prashant.kharchapaniapplication.user;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@ResponseBody
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor

public class UserController {

    private final UserService userService;

    @PostMapping()
    public ResponseEntity<?> save(@Valid @RequestBody User user) {
        User savedUser = userService.addUser(user);
        UserResponse response = new UserResponse(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                savedUser.getPhone(),
                savedUser.getBudget(),
                savedUser.getCreatedAt(),
                savedUser.isProfileComplete()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/complete-profile")
    public ResponseEntity<?> completeProfile(@Valid @RequestBody CompleteProfileRequest request) {
        userService.completeProfile(request);
        return ResponseEntity.ok().build();
    }
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UUID id = currentUser.getId();
        User user = userService.getUser(id);
        UserResponse response = new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                user.getBudget(),
                user.getCreatedAt(),
                user.isProfileComplete()
        );
        return ResponseEntity.ok(response);
    }

}
