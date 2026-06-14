package com.visa.transactionapplication.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AccountRequest(
        @NotBlank(message = "Document number is required")
                @Size(max = 20, message = "Document number must not exceed 20 characters")
                String documentNumber) {}
