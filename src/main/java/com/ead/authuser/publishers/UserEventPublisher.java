package com.ead.authuser.publishers;

import com.ead.authuser.dtos.UserEventDto;
import com.ead.authuser.enums.ActionType;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class UserEventPublisher {

    //Exchange name
    @Value("${ead.broker.exchange.userEvent}")
    private String exchangeUserEvent;

    private final RabbitTemplate rabbitTemplate;

    public UserEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishUserEvent(UserEventDto event, ActionType actionType) {
        log.info("EVENT - Publishing event {}, actionType: {}", event, actionType);
        event.setActionType(actionType.toString());
        rabbitTemplate.convertAndSend(exchangeUserEvent, "", event);
    }
}
