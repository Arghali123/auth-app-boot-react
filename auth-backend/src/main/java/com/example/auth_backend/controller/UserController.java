package com.example.auth_backend.controller;

import com.example.auth_backend.auth.payload.UserDTO;
import com.example.auth_backend.services.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping ("/api/v1/users")
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    //create user api
    @PostMapping
    public ResponseEntity<UserDTO> createUser(@RequestBody UserDTO userDTO){
     return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(userDTO));
    }

    //get all user api
    @GetMapping
    public ResponseEntity<Iterable<UserDTO>> getAllUsers()
    {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    //get user by email
    @GetMapping("/email/{email}")
    public ResponseEntity<UserDTO> getUserByEmail(@PathVariable String email)
    {
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    //delete user
    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable("userId") String userId)
    {
        userService.deleteUser(userId);
    }

    //get user by id
    @GetMapping("/{userId}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable("userId") String userId)
    {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    //update user
    @PutMapping("{userId}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable("userId") String userId,
                                              @RequestBody UserDTO userDTO)
    {
        return ResponseEntity.ok(userService.updateUser(userDTO,userId));
    }
}
