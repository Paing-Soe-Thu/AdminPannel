package com.example.WebApp.service;

import com.example.WebApp.dto.UserDto;
import com.example.WebApp.model.User;
import com.example.WebApp.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /** Register a new user. Returns the saved user or throws if email already exists. */
    public User registerUser(UserDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }
        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setPhone(dto.getPhone());
        return userRepository.save(user);
    }

    /** Authenticate a user. Returns the User on success, empty on failure. */
    public Optional<User> authenticate(String email, String rawPassword) {
        return userRepository.findByEmail(email)
                .filter(u -> passwordEncoder.matches(rawPassword, u.getPassword()));
    }

    /** Fetch all users ordered by id desc. */
    public List<User> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .sorted((a, b) -> Long.compare(b.getId(), a.getId()))
                .toList();
    }

    /** Fetch a single user by id. */
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    /** Update an existing user. If password in dto is blank, keep existing hash. */
    public User updateUser(Long id, UserDto dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));

        // Check if new email conflicts with another user
        Optional<User> existing = userRepository.findByEmail(dto.getEmail());
        if (existing.isPresent() && !existing.get().getId().equals(id)) {
            throw new IllegalArgumentException("Email is already in use by another account.");
        }

        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        return userRepository.save(user);
    }

    /** Delete a user by id. */
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    /** Total user count. */
    public long countUsers() {
        return userRepository.count();
    }
}
