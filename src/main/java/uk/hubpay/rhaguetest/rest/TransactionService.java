package uk.hubpay.rhaguetest.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.hubpay.rhaguetest.db.transaction.Transaction;
import uk.hubpay.rhaguetest.db.transaction.TransactionRepository;
import uk.hubpay.rhaguetest.db.transaction.TransactionType;
import uk.hubpay.rhaguetest.db.wallet.Wallet;
import uk.hubpay.rhaguetest.db.wallet.WalletRepository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);
    final WalletRepository walletRepository;

    final TransactionRepository transactionRepository;

    private ConcurrentHashMap<String, ReentrantLock> walletLocks = new ConcurrentHashMap<>();

    public TransactionService(WalletRepository walletRepository, TransactionRepository transactionRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    public WalletUpdateResponse creditWallet(WalletUpdate walletUpdate) {

        log.info("Attempting to credit wallet {} with amount {}", walletUpdate.walletId(), walletUpdate.amount());

        BigDecimal amount = walletUpdate.amount();
        boolean validDepositAmount = validDepositAmount(amount);
        if (!validDepositAmount) {
            String message = "Could not apply credit as amount was not in the valid range";
            log.warn(message);
            return new WalletUpdateResponse(false, message);
        }

        UUID walletId = walletUpdate.walletId();
        ReentrantLock lock = walletLocks.computeIfAbsent(walletId.toString(), s -> new ReentrantLock());
        try {
            if (lock.tryLock(5, TimeUnit.SECONDS)) {
                try {
                    Optional<Wallet> byId = walletRepository.findById(walletId);
                    if (byId.isEmpty()) {
                        String message = "Could not apply credit as wallet not found for id: " + walletId;
                        log.warn(message);
                        return new WalletUpdateResponse(false, message);
                    }

                    Wallet wallet = byId.get();
                    transactionRepository.save(Transaction.builder()
                            .amount(amount)
                            .transactionType(TransactionType.CREDIT)
                            .walletId(walletId)
                            .build());

                    BigDecimal balance = wallet.getBalance();
                    wallet.setBalance(balance.add(amount));

                    walletRepository.save(wallet);
                } finally {
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return new WalletUpdateResponse(true, "Credit Successful");
    }

    public WalletUpdateResponse debitWallet(WalletUpdate walletUpdate) {

        log.info("Attempting to debit wallet {} with amount {}", walletUpdate.walletId(), walletUpdate.amount());

        BigDecimal amount = walletUpdate.amount();
        boolean validWithdrawalAmount = validWithdrawalAmount(amount);
        if (!validWithdrawalAmount) {
            String message = "Could not apply debit as amount was not in the valid range";
            log.warn(message);
            return new WalletUpdateResponse(false, message);
        }

        UUID walletId = walletUpdate.walletId();
        ReentrantLock lock = walletLocks.computeIfAbsent(walletId.toString(), s -> new ReentrantLock());
        try {
            if (lock.tryLock(5, TimeUnit.SECONDS)) {
                try {
                    Optional<Wallet> byId = walletRepository.findById(walletId);
                    if (byId.isEmpty()) {
                        String msg = "Could not apply debit as wallet not found for id: " + walletId;
                        log.warn(msg);
                        return new WalletUpdateResponse(false, msg);
                    }

                    Wallet wallet = byId.get();

                    BigDecimal balance = wallet.getBalance();
                    BigDecimal newBalance = balance.subtract(amount);

                    if (!validBalance(newBalance)) {
                        String msg = "Could not apply debit as balance would be less than zero: " + walletId;
                        log.warn(msg);
                        return new WalletUpdateResponse(false, msg);
                    }
                    wallet.setBalance(newBalance);

                    transactionRepository.save(Transaction.builder()
                            .amount(amount)
                            .transactionType(TransactionType.DEBIT)
                            .walletId(walletId)
                            .build());
                    walletRepository.save(wallet);

                } finally {
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return new WalletUpdateResponse(true, "Debit Successful");
    }

    Page<TransactionDto> loadTransactionHistory(UUID walletId, Pageable pageable) {
        return transactionRepository.findByWalletIdOrderByTimestampAsc(walletId, pageable).map(transaction ->
                new TransactionDto(transaction.getTransactionType(), transaction.getAmount(), transaction.getTimestamp()));
    }

    Optional<WalletDto> currentBalance(UUID walletId) {
        return walletRepository.findById(walletId).map(wallet -> new WalletDto(wallet.getBalance(),wallet.getUpdated(), wallet.getCreated()));
    }

    boolean validDepositAmount(BigDecimal amount) {
        return BigDecimal.valueOf(10.00).compareTo(amount) < 1 &&
                BigDecimal.valueOf(10000.00).compareTo(amount) > -1;
    }

    boolean validWithdrawalAmount(BigDecimal amount) {
        return BigDecimal.valueOf(5000).compareTo(amount) > -1;
    }

    boolean validBalance(BigDecimal balance) {
        return BigDecimal.ZERO.compareTo(balance) < 1;
    }
}
