package com.example.auth_backend.services;

import com.example.auth_backend.auth.payload.UserDTO;

public interface AuthService {
    UserDTO registerUser(UserDTO userDTO);
}
