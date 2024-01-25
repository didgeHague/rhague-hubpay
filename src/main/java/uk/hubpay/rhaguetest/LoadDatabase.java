package uk.hubpay.rhaguetest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.hubpay.rhaguetest.db.wallet.Wallet;
import uk.hubpay.rhaguetest.db.wallet.WalletRepository;
import uk.hubpay.rhaguetest.rest.WalletUpdate;
import uk.hubpay.rhaguetest.rest.TransactionService;

import java.math.BigDecimal;
import java.util.UUID;

@Configuration
public class LoadDatabase {
    private static final Logger log = LoggerFactory.getLogger(LoadDatabase.class);

    @Bean
    CommandLineRunner initDatabase(WalletRepository walletRepository, TransactionService transactionService) {

        return args -> {

            Wallet wallet = walletRepository.save(new Wallet());
            UUID walletId = wallet.getId();
            log.info("Preloading " + wallet);

            Wallet wallet2 = walletRepository.save(new Wallet());
            UUID walletId2 = wallet2.getId();
            log.info("Preloading " + wallet2);

            transactionService.creditWallet(new WalletUpdate(walletId, BigDecimal.valueOf(2500)));
            transactionService.debitWallet(new WalletUpdate(walletId, BigDecimal.valueOf(9.99)));
            transactionService.debitWallet(new WalletUpdate(walletId, BigDecimal.valueOf(24.99)));
            transactionService.debitWallet(new WalletUpdate(walletId, BigDecimal.valueOf(1.99)));
            transactionService.debitWallet(new WalletUpdate(walletId, BigDecimal.valueOf(100)));
            transactionService.debitWallet(new WalletUpdate(walletId, BigDecimal.valueOf(8.73)));
            transactionService.debitWallet(new WalletUpdate(walletId, BigDecimal.valueOf(3.42)));
            transactionService.debitWallet(new WalletUpdate(walletId, BigDecimal.valueOf(5.43)));
            transactionService.debitWallet(new WalletUpdate(walletId, BigDecimal.valueOf(203.4)));

            log.info("Wallet Starting balance " + walletRepository.findById(walletId));
            log.info("Wallet Starting balance " + walletRepository.findById(walletId2));
        };
    }
}
