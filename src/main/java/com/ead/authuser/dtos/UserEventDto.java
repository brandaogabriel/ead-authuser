package com.ead.authuser.dtos;

import com.ead.authuser.models.UserModel;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.UUID;

@Data
public class UserEventDto {

    private UUID userId;
    private String username;
    private String email;
    private String fullName;
    private String userStatus;
    private String userType;
    private String phoneNumber;
    private String cpf;
    private String imageUrl;
    private String actionType;

    public static UserEventDto toModel(UserModel userModel) {
        UserEventDto event = new UserEventDto();
        BeanUtils.copyProperties(userModel, event);
        event.setUserType(userModel.getUserType().toString());
        event.setUserStatus(userModel.getUserStatus().toString());
        return event;
    }

}
