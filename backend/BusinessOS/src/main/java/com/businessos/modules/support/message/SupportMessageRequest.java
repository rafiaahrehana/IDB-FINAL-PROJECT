package com.businessos.modules.support.message;

import jakarta.validation.constraints.*;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class SupportMessageRequest {

    @NotNull(message = "Ticket ID is required")
    private Long ticketId;

    @NotBlank(message = "Message is required")
    private String message;

    @Builder.Default
    private boolean isInternal = false;

    private String attachmentUrl;
    private String attachmentFileName;

    // missing fields
    private Long sentByUserId;
    @Builder.Default
    private String messageType = "TEXT";
    @Builder.Default
    private boolean isResolution = false;
}
