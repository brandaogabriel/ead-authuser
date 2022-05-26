package com.ead.authuser.controllers;

import com.ead.authuser.configs.security.JwtProvider;
import com.ead.authuser.dtos.JwtDto;
import com.ead.authuser.dtos.LoginDto;
import com.ead.authuser.dtos.UserDto;
import com.ead.authuser.enums.RoleType;
import com.ead.authuser.enums.UserStatus;
import com.ead.authuser.enums.UserType;
import com.ead.authuser.models.RoleModel;
import com.ead.authuser.models.UserModel;
import com.ead.authuser.services.RoleService;
import com.ead.authuser.services.UserService;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Log4j2
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private final UserService userService;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final AuthenticationManager authenticationManager;

    public AuthenticationController(UserService userService, RoleService roleService, PasswordEncoder passwordEncoder, JwtProvider jwtProvider, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
        this.authenticationManager = authenticationManager;
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

        RoleModel roleModel = roleService.findByRoleName(RoleType.ROLE_STUDENT)
                .orElseThrow(() -> new RuntimeException("Error: Role is not Found"));

        request.setPassword(passwordEncoder.encode(request.getPassword()));
        var userModel = new UserModel();
        BeanUtils.copyProperties(request, userModel);
        userModel.setUserStatus(UserStatus.ACTIVE);
        userModel.setUserType(UserType.STUDENT);
        userModel.setCreationDate(LocalDateTime.now(ZoneId.of("UTC")));
        userModel.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));
        userModel.getRoles().add(roleModel);

        userService.saveUserAndPublishEvent(userModel);
        log.debug("POST registerUser userId Saved {}", userModel.getUserId());
        log.info("User saved successfully, userId: {}", userModel.getUserId());

        return ResponseEntity.status(HttpStatus.CREATED).body(userModel);
    }

    @PostMapping("/login")
    public ResponseEntity<JwtDto> authenticateUser(@Valid @RequestBody LoginDto loginDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtProvider.generateJwt(authentication);
        return ResponseEntity.status(HttpStatus.OK).body(new JwtDto(token));
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
