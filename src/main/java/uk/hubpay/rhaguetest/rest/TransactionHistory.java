package uk.hubpay.rhaguetest.rest;

import uk.hubpay.rhaguetest.db.transaction.Transaction;

import java.util.List;

public record TransactionHistory(List<TransactionDto> transactions, int startPage, int endPage) {
}
