package com.qlda.manage_project.modules.sprint.listener;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.qlda.manage_project.modules.sprint.event.SprintStartedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class SprintEventListener {

    @Async
    @EventListener
    public void handleSprintStarted(SprintStartedEvent event) {
        log.info("Notification Module nhận được event: Sprint {} vừa bắt đầu", event.getSprintName());
    }
}
