package uk.hubpay.rhaguetest.rest;

import java.math.BigDecimal;
import java.util.UUID;

public record WalletUpdate(UUID walletId, BigDecimal amount) {
}
