package com.businessos.modules.project.meeting;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MeetingRequest {

    @NotBlank(message = "Meeting title is required")
    private String title;

    private String description;

    private Long organizerId;

    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String location;
}
