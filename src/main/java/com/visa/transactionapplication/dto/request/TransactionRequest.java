package com.visa.transactionapplication.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record TransactionRequest(
        @NotNull(message = "Account ID is required") Long accountId,
        @NotNull(message = "Operation type ID is required") Long operationTypeId,
        @NotNull(message = "Amount is required") @Positive(message = "Amount must be a positive value")
                BigDecimal amount) {}
