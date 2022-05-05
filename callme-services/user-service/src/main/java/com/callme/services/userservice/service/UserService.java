package com.callme.services.userservice.service;

import com.callme.services.userservice.exception.DuplicateUsernameException;
import com.callme.services.userservice.exception.UserNotFoundException;
import com.callme.services.userservice.model.AppUser;
import com.callme.services.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Transactional
    public AppUser addNewUser(AppUser appUser) throws DuplicateUsernameException {
        // Check for duplicate username
        Optional<AppUser> optionalAppUser = userRepository.findUserByUsername(appUser.getUsername());
        if (optionalAppUser.isPresent()) {
            throw new DuplicateUsernameException();
        }
        // Encode the password before saving
        appUser.setPassword(passwordEncoder.encode(appUser.getPassword()));
        // Save the new user
        return userRepository.save(appUser);
    }

    public boolean isValidLoginRequest(String username, String password) {
        // Retrieve user by username and get their encoded password
        AppUser appUser = userRepository.findUserByUsername(username)
                .orElseThrow(UserNotFoundException::new);
        String encodedPass = appUser.getPassword();
        // Check for matching password
        return passwordEncoder.matches(password, encodedPass);
    }

    public Optional<AppUser> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<AppUser> getUserByUsername(String username) {
        return userRepository.findUserByUsername(username);
    }
}
