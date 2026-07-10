package com.businessos.modules.servicedesk.servicerequest;

import com.businessos.enums.ServiceRequestPriority;
import com.businessos.shared.payment.PaymentChoice;
import com.businessos.shared.payment.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CreateServiceRequestRequest {

    @NotBlank(message = "Request title is required")
    @Size(max = 255)
    private String title;

    private String description;

    @NotNull(message = "Service ID is required")
    private Long hubServiceId;

    private ServiceRequestPriority priority;

    @DecimalMin(value = "0.00")
    private BigDecimal agreedPrice;

    private LocalDateTime slaDeadline;

    /**
     * Optional — if the client is raising this request under an existing
     * active subscription, provide the subscription ID here.
     * The service will validate quota and decrement requestsUsed.
     * When provided, agreedPrice is set to ZERO (included in package).
     */
    private Long subscriptionId;

    @NotNull(message = "Payment choice is required")
    private PaymentChoice paymentChoice;

    private PaymentMethod paymentMethod;
}
