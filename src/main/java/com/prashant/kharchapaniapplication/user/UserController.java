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
    public User save(@Valid @RequestBody User user) {
        userService.addUser(user);
        return user;
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
        return ResponseEntity.ok(userService.getUser(id));
    }

}
