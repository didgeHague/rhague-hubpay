package uk.hubpay.rhaguetest.rest;

import uk.hubpay.rhaguetest.db.transaction.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionDto(TransactionType transactionType, BigDecimal amount, LocalDateTime timestamp) {
}
