package com.ead.authuser.controllers;

import com.ead.authuser.clients.CourseClient;
import com.ead.authuser.dtos.CourseDto;
import com.ead.authuser.dtos.UserCourseDto;
import com.ead.authuser.models.UserCourseModel;
import com.ead.authuser.models.UserModel;
import com.ead.authuser.services.UserCourseService;
import com.ead.authuser.services.UserService;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;
import java.util.UUID;

@Log4j2
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserCourseController {

    private final CourseClient courseClient;
    private final UserService userService;
    private final UserCourseService userCourseService;

    public UserCourseController(CourseClient courseClient, UserService userService, UserCourseService userCourseService) {
        this.courseClient = courseClient;
        this.userService = userService;
        this.userCourseService = userCourseService;
    }

    @GetMapping("/api/v1/users/{userId}/courses")
    public ResponseEntity<Page<CourseDto>> getAllCoursesByUser(
            @PageableDefault(page = 0, size = 10, sort = "courseId", direction = Sort.Direction.ASC) Pageable pageable,
            @PathVariable(value = "userId") UUID userId) {

        return ResponseEntity.status(HttpStatus.OK).body(courseClient.getAllCoursesByUser(userId, pageable));
    }

    @PostMapping("/api/v1/users/{userId}/courses/subscription")
    public ResponseEntity<Object> saveSubscriptionUserInCourse(@PathVariable(value = "userId") UUID userId,
                                                               @RequestBody @Valid UserCourseDto request) {
        Optional<UserModel> possibleUserModel = userService.findById(userId);
        if (possibleUserModel.isEmpty()) {
            log.warn("POST saveSubscriptionUserInCourse userId {} NOT FOUND", userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        if (userCourseService.existsByUserAndCourseId(possibleUserModel.get(), request.getCourseId())) {
            log.warn("POST saveSubscriptionUserInCourse userId {} subscription already exists", userId);
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body("Error: subscription already exists");
        }

        var userCourseModel = userCourseService.save(new UserCourseModel(null, possibleUserModel.get(), request.getCourseId()));
        return ResponseEntity.status(HttpStatus.CREATED).body(userCourseModel);
    }
}
