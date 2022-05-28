package com.ead.authuser.services.impl;

import com.ead.authuser.dtos.UserEventDto;
import com.ead.authuser.enums.ActionType;
import com.ead.authuser.models.UserModel;
import com.ead.authuser.publishers.UserEventPublisher;
import com.ead.authuser.repositories.UserRepository;
import com.ead.authuser.services.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserEventPublisher userEventPublisher;

    public UserServiceImpl(UserRepository userRepository, UserEventPublisher userEventPublisher) {
        this.userRepository = userRepository;
        this.userEventPublisher = userEventPublisher;
    }

    @Override
    public Page<UserModel> findAll(Specification<UserModel> spec, Pageable pageable) {
        return userRepository.findAll(spec, pageable);
    }

    @Override
    public Optional<UserModel> findById(UUID id) {
        return userRepository.findById(id);
    }

    @Transactional
    @Override
    public void deleteUser(UserModel userModel) {
        UserEventDto event = UserEventDto.toModel(userModel);
        userEventPublisher.publishUserEvent(event, ActionType.DELETE);
        userRepository.delete(userModel);
    }

    @Override
    public UserModel save(UserModel userModel) {
        return userRepository.save(userModel);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional
    @Override
    public UserModel saveUserAndPublishEvent(UserModel userModel) {
        userModel = save(userModel);
        UserEventDto event = UserEventDto.toModel(userModel);
        userEventPublisher.publishUserEvent(event, ActionType.CREATE);
        return userModel;
    }

    @Override
    public UserModel updateUser(UserModel userModel) {
        userModel = save(userModel);
        UserEventDto event = UserEventDto.toModel(userModel);
        userEventPublisher.publishUserEvent(event, ActionType.UPDATE);
        return userModel;
    }

    @Override
    public UserModel updatePassword(UserModel userModel) {
        return save(userModel);
    }

    @Override
    public Optional<UserModel> findByUserId(UUID userId) {
        return userRepository.findByUserId(userId);
    }
}
