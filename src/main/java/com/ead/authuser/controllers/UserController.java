package com.ead.authuser.controllers;

import com.ead.authuser.dtos.UserDto;
import com.ead.authuser.models.UserModel;
import com.ead.authuser.services.UserService;
import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserModel>> getAllUsers() {
        return ResponseEntity.status(HttpStatus.OK).body(userService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getUserById(@PathVariable(value = "id") UUID id) {
        Optional<UserModel> possibleUserModel = userService.findById(id);
        if (possibleUserModel.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        return ResponseEntity.status(HttpStatus.OK).body(possibleUserModel.get());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteUserById(@PathVariable(value = "id") UUID id) {
        Optional<UserModel> possibleUserModel = userService.findById(id);
        if (possibleUserModel.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        userService.deleteUser(possibleUserModel.get());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateUserById(@PathVariable(value = "id") UUID id,
                                                 @RequestBody
                                                 @Validated(UserDto.UserView.UserPut.class)
                                                 @JsonView(UserDto.UserView.UserPut.class) UserDto request) {
        Optional<UserModel> possibleUserModel = userService.findById(id);
        if (possibleUserModel.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        var userModel = possibleUserModel.get();
        userModel.setFullname(request.getFullname());
        userModel.setPhoneNumber(request.getPhoneNumber());
        userModel.setCpf(request.getCpf());
        userModel.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));
        userService.save(userModel);

        return ResponseEntity.status(HttpStatus.OK).body(userModel);
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<Object> updateUserPasswordById(@PathVariable(value = "id") UUID id,
                                                         @RequestBody
                                                         @Validated(UserDto.UserView.PasswordPut.class)
                                                         @JsonView(UserDto.UserView.PasswordPut.class) UserDto request) {
        Optional<UserModel> possibleUserModel = userService.findById(id);
        if (possibleUserModel.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        if (!possibleUserModel.get().getPassword().equals(request.getOldPassword())) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body("Error: Mismatched old password.");
        }

        var userModel = possibleUserModel.get();
        userModel.setPassword(request.getPassword());
        userModel.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));
        userService.save(userModel);
        return ResponseEntity.status(HttpStatus.OK).body("Password update successfully.");
    }

    @PutMapping("/{id}/image")
    public ResponseEntity<Object> updateUserImageById(@PathVariable(value = "id") UUID id,
                                                      @RequestBody
                                                      @Validated(UserDto.UserView.ImagePut.class)
                                                      @JsonView(UserDto.UserView.ImagePut.class) UserDto request) {
        Optional<UserModel> possibleUserModel = userService.findById(id);
        if (possibleUserModel.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        var userModel = possibleUserModel.get();
        userModel.setImageUrl(request.getImageUrl());
        userModel.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));
        userService.save(userModel);
        return ResponseEntity.status(HttpStatus.OK).body(userModel);
    }
}
