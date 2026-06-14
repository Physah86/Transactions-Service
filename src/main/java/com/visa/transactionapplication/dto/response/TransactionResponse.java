package com.visa.transactionapplication.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionResponse(
        Long transactionId, Long accountId, Long operationTypeId, BigDecimal amount, Instant eventDate) {}
