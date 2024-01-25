package uk.hubpay.rhaguetest.rest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
public class CustomerController {

    final TransactionService transactionService;

    public CustomerController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/wallet/credit")
    ResponseEntity<WalletUpdateResponse> credit(@RequestBody WalletUpdate walletUpdate) {
        return ResponseEntity.ok(transactionService.creditWallet(walletUpdate));
    }

    @PostMapping("/wallet/debit")
    ResponseEntity<WalletUpdateResponse> debit(@RequestBody WalletUpdate walletUpdate) {
        return ResponseEntity.ok(transactionService.debitWallet(walletUpdate));
    }

    @GetMapping("/wallet/{uuid}/transactions")
    Page<TransactionDto> transactionHistory(@PathVariable("uuid") UUID walletId, Pageable p) {
        return transactionService.loadTransactionHistory(walletId, p);
    }

    @GetMapping("/wallet/{uuid}/balance")
    ResponseEntity<WalletDto> balance(@PathVariable("uuid") UUID walletId) {
        Optional<WalletDto> walletDto = transactionService.currentBalance(walletId);
        return ResponseEntity.of(walletDto);
    }
}
