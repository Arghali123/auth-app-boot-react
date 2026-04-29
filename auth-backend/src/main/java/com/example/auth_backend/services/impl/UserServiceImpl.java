package com.example.auth_backend.services.impl;

import com.example.auth_backend.auth.payload.UserDTO;
import com.example.auth_backend.services.UserService;

public class UserServiceImpl implements UserService {
    @Override
    public UserDTO createUser(UserDTO userDTO) {
        return null;
    }

    @Override
    public UserDTO getUserByEmail(String email) {
        return null;
    }

    @Override
    public UserDTO updateUser(UserDTO userDTO, String userId) {
        return null;
    }

    @Override
    public void deleteUser(String userId) {

    }

    @Override
    public Iterable<UserDTO> getAllUsers() {
        return null;
    }
}
