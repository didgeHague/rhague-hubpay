package uk.hubpay.rhaguetest.rest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WalletDto(BigDecimal balance, LocalDateTime updated, LocalDateTime created) {
}
