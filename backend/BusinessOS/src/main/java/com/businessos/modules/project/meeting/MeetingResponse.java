package com.businessos.modules.project.meeting;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MeetingResponse {

    private Long id;
    private String title;
    private String description;
    private Long organizerId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String location;
}
