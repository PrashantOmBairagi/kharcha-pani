package com.prashant.kharchapaniapplication.user;

import com.prashant.kharchapaniapplication.exception.ConflictException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

//    public User addUser(User user) {
//        return userRepository.save(user);    Auth service handles creating user at time of registration.
//    }
    public void completeProfile(CompleteProfileRequest request) {

        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        currentUser.setFirstName(request.getFirstName());
        currentUser.setLastName(request.getLastName());
        currentUser.setBudget(request.getBudget());
        currentUser.setPhone(request.getPhone());
        currentUser.setProfileComplete(true);
        try{
            userRepository.save(currentUser);
        }
        catch (DataIntegrityViolationException e){
            e.printStackTrace();
            throw new ConflictException(e.getMessage());
        }
    }
    public User getUser(UUID id) {
        return userRepository.findById(id).orElseThrow();
    }


}
