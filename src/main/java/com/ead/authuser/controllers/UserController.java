package com.ead.authuser.controllers;

import com.ead.authuser.configs.security.AuthenticationCurrentUserService;
import com.ead.authuser.configs.security.UserDetailsImpl;
import com.ead.authuser.dtos.UserDto;
import com.ead.authuser.models.UserModel;
import com.ead.authuser.services.UserService;
import com.ead.authuser.specifications.SpecificationTemplate;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Log4j2
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final AuthenticationCurrentUserService authenticationCurrentUserService;

    public UserController(UserService userService, AuthenticationCurrentUserService authenticationCurrentUserService) {
        this.userService = userService;
        this.authenticationCurrentUserService = authenticationCurrentUserService;
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping
    public ResponseEntity<Page<UserModel>> getAllUsers(
            SpecificationTemplate.UserSpec spec,
            @PageableDefault(page = 0, size = 10, sort = "userId", direction = Sort.Direction.ASC) Pageable pageable,
            Authentication authentication) {
        UserDetails principal = (UserDetailsImpl) authentication.getPrincipal();
        log.info("Authentication {}", principal.getUsername());
        log.debug("GET getAllUsers users with pagination");

        Page<UserModel> userModelPage = userService.findAll(spec, pageable);

        if (userModelPage.hasContent()) {
            for (UserModel model : userModelPage.toList()) {
                model.add(linkTo(methodOn(UserController.class).getUserById(model.getUserId())).withSelfRel());
            }
        }

        return ResponseEntity.status(HttpStatus.OK).body(userModelPage);
    }

    @PreAuthorize("hasAnyRole('STUDENT')")
    @GetMapping("/{id}")
    public ResponseEntity<Object> getUserById(@PathVariable(value = "id") UUID id) {
        log.debug("GET getUserById userId {}", id);
        UUID currentUserId = authenticationCurrentUserService.getCurrentUser().getUserId();
        if (currentUserId.equals(id)) {
            Optional<UserModel> possibleUserModel = userService.findById(id);
            if (possibleUserModel.isEmpty()) {
                log.warn("GET getUserById userId {} NOT FOUND", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }
            return ResponseEntity.status(HttpStatus.OK).body(possibleUserModel.get());
        } else {
            throw new AccessDeniedException("Forbidden");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteUserById(@PathVariable(value = "id") UUID id) {
        log.debug("DELETE deleteUserById userId {} received", id);

        Optional<UserModel> possibleUserModel = userService.findById(id);
        if (possibleUserModel.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        userService.deleteUser(possibleUserModel.get());

        log.debug("DELETE deleteUserById, userId {}", id);
        log.info("User deleted successfully");

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateUserById(@PathVariable(value = "id") UUID id,
                                                 @RequestBody
                                                 @Validated(UserDto.UserView.UserPut.class)
                                                 @JsonView(UserDto.UserView.UserPut.class) UserDto request) {
        log.debug("PUT updateUserById userDto received {}", request.toString());
        Optional<UserModel> possibleUserModel = userService.findById(id);
        if (possibleUserModel.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        var userModel = possibleUserModel.get();
        userModel.setFullName(request.getFullName());
        userModel.setPhoneNumber(request.getPhoneNumber());
        userModel.setCpf(request.getCpf());
        userModel.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));
        userService.updateUser(userModel);

        log.debug("PUT updateUserById userId Saved {}", userModel.getUserId());
        log.info("User updated successfully, userId: {}", userModel.getUserId());

        return ResponseEntity.status(HttpStatus.OK).body(userModel);
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<Object> updateUserPasswordById(@PathVariable(value = "id") UUID id,
                                                         @RequestBody
                                                         @Validated(UserDto.UserView.PasswordPut.class)
                                                         @JsonView(UserDto.UserView.PasswordPut.class) UserDto request) {
        log.info("PUT updateUserPasswordById userId {}", id);

        Optional<UserModel> possibleUserModel = userService.findById(id);
        if (possibleUserModel.isEmpty()) {
            log.warn("PUT updateUserPasswordById userId {} NOT FOUND", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        if (!possibleUserModel.get().getPassword().equals(request.getOldPassword())) {
            log.warn("PUT updateUserPasswordById Mismatched old password, userId {}", id);
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body("Error: Mismatched old password.");
        }

        var userModel = possibleUserModel.get();
        userModel.setPassword(request.getPassword());
        userModel.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));
        userService.updatePassword(userModel);

        log.info("User updated successfully, userId: {}", userModel.getUserId());

        return ResponseEntity.status(HttpStatus.OK).body("Password update successfully.");
    }

    @PutMapping("/{id}/image")
    public ResponseEntity<Object> updateUserImageById(@PathVariable(value = "id") UUID id,
                                                      @RequestBody
                                                      @Validated(UserDto.UserView.ImagePut.class)
                                                      @JsonView(UserDto.UserView.ImagePut.class) UserDto request) {
        log.info("PUT updateUserImageById userId {}", id);

        Optional<UserModel> possibleUserModel = userService.findById(id);
        if (possibleUserModel.isEmpty()) {
            log.warn("PUT updateUserPasswordById userId {} NOT FOUND", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        var userModel = possibleUserModel.get();
        userModel.setImageUrl(request.getImageUrl());
        userModel.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));
        userService.updateUser(userModel);

        log.debug("PUT updateUserImageById userId Saved {}", userModel.getUserId());
        log.info("User updated successfully, userId: {}", userModel.getUserId());

        return ResponseEntity.status(HttpStatus.OK).body(userModel);
    }
}
