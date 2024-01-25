package uk.hubpay.rhaguetest.rest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.hubpay.rhaguetest.db.transaction.Transaction;
import uk.hubpay.rhaguetest.db.transaction.TransactionRepository;
import uk.hubpay.rhaguetest.db.transaction.TransactionType;
import uk.hubpay.rhaguetest.db.wallet.Wallet;
import uk.hubpay.rhaguetest.db.wallet.WalletRepository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    TransactionRepository transactionRepository;

    @Mock
    WalletRepository walletRepository;

    private TransactionService service;

    @BeforeEach
    void setUp() {
        service = new TransactionService(walletRepository, transactionRepository);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    public void testValidDeposit_greaterThan10k() {
        boolean valid = service.validDepositAmount(BigDecimal.valueOf(10000.01));
        assertFalse(valid);
    }

    @Test
    public void testValidDeposit_equalTo10k() {
        boolean valid = service.validDepositAmount(BigDecimal.valueOf(10000.00));
        assertTrue(valid);
    }

    @Test
    public void testValidDeposit_lessThan10() {
        boolean valid = service.validDepositAmount(BigDecimal.valueOf(9.99));
        assertFalse(valid);
    }

    @Test
    public void testValidDeposit_equalTo10() {
        boolean valid = service.validDepositAmount(BigDecimal.valueOf(10));
        assertTrue(valid);
    }


    @Test
    public void testValidWithdrawalAmount_equalTo5000() {
        boolean valid = service.validWithdrawalAmount(BigDecimal.valueOf(5000));
        assertTrue(valid);
    }

    @Test
    public void testValidWithdrawalAmount_lessThan5000() {
        boolean valid = service.validWithdrawalAmount(BigDecimal.valueOf(4999.99));
        assertTrue(valid);
    }

    @Test
    public void testValidWithdrawalAmount_greaterThan5000() {
        boolean valid = service.validWithdrawalAmount(BigDecimal.valueOf(5000.01));
        assertFalse(valid);
    }

    @Test
    public void testValidBalance_positive() {
        boolean valid = service.validBalance(BigDecimal.valueOf(0.01));
        assertTrue(valid);
    }

    @Test
    public void testValidBalance_zero() {
        boolean valid = service.validBalance(BigDecimal.valueOf(0.00));
        assertTrue(valid);
    }

    @Test
    public void testValidBalance_negative() {
        boolean valid = service.validBalance(BigDecimal.valueOf(-0.01));
        assertFalse(valid);
    }

    @Test
    public void testCreditWallet() {
        UUID walletId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.valueOf(100);

        Wallet wallet = new Wallet();

        Transaction transaction = Transaction.builder()
                .amount(amount)
                .transactionType(TransactionType.CREDIT)
                .walletId(walletId)
                .build();

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));

        WalletUpdateResponse walletUpdateResponse = service.creditWallet(new WalletUpdate(walletId, amount));
        assertTrue(walletUpdateResponse.success());

        verify(transactionRepository, times(1)).save(transaction);
        verify(walletRepository, times(1)).save(wallet);
    }

    @Test
    public void testCreditWallet_invalidAmount() {
        UUID walletId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.valueOf(10000.01);

        Wallet wallet = new Wallet();

        Transaction transaction = Transaction.builder()
                .amount(amount)
                .transactionType(TransactionType.CREDIT)
                .walletId(walletId)
                .build();

        WalletUpdateResponse walletUpdateResponse = service.creditWallet(new WalletUpdate(walletId, amount));
        assertFalse(walletUpdateResponse.success());
        assertEquals("Could not apply credit as amount was not in the valid range", walletUpdateResponse.message());

        verify(transactionRepository, never()).save(transaction);
        verify(walletRepository, never()).save(wallet);
    }

    @Test
    public void testCreditWallet_walletNotFound() {
        UUID walletId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.valueOf(1000);

        Wallet wallet = new Wallet();

        Transaction transaction = Transaction.builder()
                .amount(amount)
                .transactionType(TransactionType.CREDIT)
                .walletId(walletId)
                .build();

        when(walletRepository.findById(walletId)).thenReturn(Optional.empty());

        WalletUpdateResponse walletUpdateResponse = service.creditWallet(new WalletUpdate(walletId, amount));
        assertFalse(walletUpdateResponse.success());
        assertEquals("Could not apply credit as wallet not found for id: " + walletId, walletUpdateResponse.message());

        verify(transactionRepository, never()).save(transaction);
        verify(walletRepository, never()).save(wallet);
    }

    @Test
    public void testDebitWallet() {
        UUID walletId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.valueOf(100);

        Wallet wallet = new Wallet();
        wallet.setBalance(amount);

        Wallet updatedWallet = new Wallet();

        Transaction transaction = Transaction.builder()
                .amount(amount)
                .transactionType(TransactionType.DEBIT)
                .walletId(walletId)
                .build();

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));

        WalletUpdateResponse walletUpdateResponse = service.debitWallet(new WalletUpdate(walletId, amount));
        assertTrue(walletUpdateResponse.success());

        verify(transactionRepository, times(1)).save(transaction);
        verify(walletRepository, times(1)).save(updatedWallet);
    }

    @Test
    public void testDebitWallet_invalidAmount() {
        UUID walletId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.valueOf(10000.01);

        Wallet wallet = new Wallet();

        Transaction transaction = Transaction.builder()
                .amount(amount)
                .transactionType(TransactionType.DEBIT)
                .walletId(walletId)
                .build();

        WalletUpdateResponse walletUpdateResponse = service.debitWallet(new WalletUpdate(walletId, amount));
        assertFalse(walletUpdateResponse.success());
        assertEquals("Could not apply debit as amount was not in the valid range", walletUpdateResponse.message());

        verify(transactionRepository, never()).save(transaction);
        verify(walletRepository, never()).save(wallet);
    }

    @Test
    public void testDebitWallet_walletNotFound() {
        UUID walletId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.valueOf(1000);

        Wallet wallet = new Wallet();

        Transaction transaction = Transaction.builder()
                .amount(amount)
                .transactionType(TransactionType.DEBIT)
                .walletId(walletId)
                .build();

        when(walletRepository.findById(walletId)).thenReturn(Optional.empty());

        WalletUpdateResponse walletUpdateResponse = service.debitWallet(new WalletUpdate(walletId, amount));
        assertFalse(walletUpdateResponse.success());
        assertEquals("Could not apply debit as wallet not found for id: " + walletId, walletUpdateResponse.message());

        verify(transactionRepository, never()).save(transaction);
        verify(walletRepository, never()).save(wallet);
    }
}