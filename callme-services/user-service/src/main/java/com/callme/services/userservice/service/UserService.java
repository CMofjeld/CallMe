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

    private void logDBQuery(String queryDescription) {
        System.out.println("Querying database to %s".formatted(queryDescription));
    }

    private void logDBStore(String storedObject) {
        System.out.println("Storing %s in database".formatted(storedObject));
    }

    @Transactional
    public AppUser addNewUser(AppUser appUser) throws DuplicateUsernameException {
        // Check for duplicate username
        logDBQuery("check if user with name %s already exists".formatted(appUser.getUsername()));
        Optional<AppUser> optionalAppUser = userRepository.findUserByUsername(appUser.getUsername());
        if (optionalAppUser.isPresent()) {
            throw new DuplicateUsernameException();
        }
        // Encode the password before saving
        appUser.setPassword(passwordEncoder.encode(appUser.getPassword()));
        // Save the new user
        logDBStore("new user");
        return userRepository.save(appUser);
    }

    public boolean isValidLoginRequest(String username, String password) {
        // Retrieve user by username and get their encoded password
        logDBQuery("retrieve user information for %s".formatted(username));
        AppUser appUser = userRepository.findUserByUsername(username)
                .orElseThrow(UserNotFoundException::new);
        String encodedPass = appUser.getPassword();
        // Check for matching password
        System.out.println("Checking that hashed passwords match");
        return passwordEncoder.matches(password, encodedPass);
    }

    public Optional<AppUser> getUserById(Long id) {
        logDBQuery("retrieve user with id %d".formatted(id));
        return userRepository.findById(id);
    }

    public Optional<AppUser> getUserByUsername(String username) {
        logDBQuery("retrieve user with name %s".formatted(username));
        return userRepository.findUserByUsername(username);
    }

    public boolean existsById(Long id) {
        logDBQuery("check if user with id %d exists".formatted(id));
        return userRepository.existsById(id);
    }
}
