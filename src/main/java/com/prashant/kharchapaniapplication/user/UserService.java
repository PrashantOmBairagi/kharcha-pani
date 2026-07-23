package com.prashant.kharchapaniapplication.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public void addUser(User user) {
        userRepository.save(user);
    }
    public void completeProfile(CompleteProfileRequest request) {

        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        currentUser.setFirstName(request.getFirstName());
        currentUser.setLastName(request.getLastName());
        currentUser.setBudget(request.getBudget());
        currentUser.setPhone(request.getPhone());
        currentUser.setProfileComplete(true);
        userRepository.save(currentUser);
    }
    public User getUser(UUID id) {
        return userRepository.findById(id).orElseThrow();
    }


}
