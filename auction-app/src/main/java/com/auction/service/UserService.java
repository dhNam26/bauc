package com.auction.service;

import com.auction.model.User;
import com.auction.repository.UserRepository;
import com.auction.util.IdGenerator;
import com.auction.util.ValidationUtil;

import java.util.List;

public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User register(String username, String email) {
        ValidationUtil.require(username != null && !username.isBlank(), "Username must not be blank");
        ValidationUtil.require(email != null && !email.isBlank(), "Email must not be blank");
        User user = new User(IdGenerator.newId(), username, email);
        return userRepository.save(user);
    }

    public User getById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
    }

    public List<User> listUsers() {
        return userRepository.findAll();
    }
}
