package com.ead.authuser.controllers;

import com.ead.authuser.dtos.UserDto;
import com.ead.authuser.enums.UserStatus;
import com.ead.authuser.enums.UserType;
import com.ead.authuser.models.UserModel;
import com.ead.authuser.services.UserService;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Log4j2
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private final UserService userService;

    public AuthenticationController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signup")
    public ResponseEntity<Object> registerUser(
            @RequestBody
            @Validated(UserDto.UserView.RegistrationPost.class)
            @JsonView(UserDto.UserView.RegistrationPost.class) UserDto request) {
        log.debug("POST registerUser userDto received {}", request.toString());

        if (userService.existsByUsername(request.getUsername())) {
            log.warn("POST registerUser Username {} is already taken!", request.getUsername());
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body("Error: Username is already taken!");
        }
        if (userService.existsByEmail(request.getEmail())) {
            log.warn("POST registerUser Email {} is already taken!", request.getEmail());
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body("Error: Email is already taken!");
        }

        var userModel = new UserModel();
        BeanUtils.copyProperties(request, userModel);
        userModel.setUserStatus(UserStatus.ACTIVE);
        userModel.setUserType(UserType.STUDENT);
        userModel.setCreationDate(LocalDateTime.now(ZoneId.of("UTC")));
        userModel.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));

        userService.save(userModel);
        log.debug("POST registerUser userModel Saved {}", userModel.toString());
        log.info("User saved successfully, userId: {}", userModel.getUserId());

        return ResponseEntity.status(HttpStatus.CREATED).body(userModel);
    }

    @GetMapping("/")
    public String index() {
        log.trace("TRACE LOG");
        log.debug("DEBUG LOG");
        log.info("INFO LOG");
        log.warn("WARN LOG");
        log.error("ERROR LOG");

        return "Logging Spring Boot";
    }
}
