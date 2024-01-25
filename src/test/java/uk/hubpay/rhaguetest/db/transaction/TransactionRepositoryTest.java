package uk.hubpay.rhaguetest.db.transaction;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.hubpay.rhaguetest.db.transaction.TransactionType.CREDIT;
import static uk.hubpay.rhaguetest.db.transaction.TransactionType.DEBIT;

@ExtendWith(SpringExtension.class)
@DataJpaTest
class TransactionRepositoryTest {

    @Autowired
    TransactionRepository repository;

    private UUID transactionId;
    private UUID walletId = UUID.randomUUID();
    private UUID walletId2 = UUID.randomUUID();

    @BeforeEach
    public void setup(){
        Transaction transaction = repository.save(Transaction.builder()
                .walletId(walletId)
                .amount(BigDecimal.valueOf(1000))
                .transactionType(CREDIT)
                .build());
        repository.save(Transaction.builder()
                .walletId(walletId)
                .amount(BigDecimal.valueOf(99.99))
                .transactionType(DEBIT)
                .build());

        transactionId = transaction.getId();

        repository.save(Transaction.builder()
                .walletId(walletId2)
                .amount(BigDecimal.valueOf(1000))
                .transactionType(DEBIT)
                .build());
    }


    @AfterEach
    public void tearDown(){
        repository.deleteAll();
    }

    @Test
    public void testFindAll(){
        List<Transaction> transactions = repository.findAll();
        assertEquals(3, transactions.size());
    }

    @Test
    public void testFindById(){
        Optional<Transaction> byId = repository.findById(transactionId);
        assertTrue(byId.isPresent());

        Transaction test = byId.get();
        assertEquals(transactionId, test.getId());
        assertEquals(BigDecimal.valueOf(1000), test.getAmount());
        assertEquals(walletId, test.getWalletId());
        assertEquals(CREDIT, test.getTransactionType());
    }

    @Test
    public void testFindByWalletId(){
        List<Transaction> byWalletId = repository.findByWalletId(walletId);
        assertEquals(2, byWalletId.size());

        Transaction transaction = byWalletId.get(0);
        assertEquals(transactionId, transaction.getId());
        assertEquals(BigDecimal.valueOf(1000), transaction.getAmount());
        assertEquals(CREDIT, transaction.getTransactionType());
        assertEquals(walletId, transaction.getWalletId());


        Transaction transaction2 = byWalletId.get(1);
        assertEquals(BigDecimal.valueOf(99.99), transaction2.getAmount());
        assertEquals(DEBIT, transaction2.getTransactionType());
        assertEquals(walletId, transaction2.getWalletId());
    }
}