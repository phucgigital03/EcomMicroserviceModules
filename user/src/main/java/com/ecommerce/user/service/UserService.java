package com.ecommerce.user.service;

import com.ecommerce.user.dto.UserRequest;
import com.ecommerce.user.dto.UserResponse;
import com.ecommerce.user.model.User;
import com.ecommerce.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    public List<UserResponse> fetchAllUsers() {
        return userRepository.findAll().stream().map(user -> {
            return modelMapper.map(user, UserResponse.class);
        }).collect(Collectors.toList());
    }

    public void addUser(UserRequest userRequest) {
        User user = modelMapper.map(userRequest, User.class);
        userRepository.save(user);
    }

    public Optional<UserResponse> fetchUser(String id) {
        return userRepository.findById(id).map(user -> {
            return modelMapper.map(user, UserResponse.class);
        });
    }

    public boolean updateUser(String id, UserRequest userRequest) {
        return userRepository.findById(id)
                .map(existingUser -> {
                    existingUser.setFirstName(userRequest.getFirstName());
                    existingUser.setLastName(userRequest.getLastName());
                    userRepository.save(existingUser);
                    return true;
                }).orElse(false);
    }
}
