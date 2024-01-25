package uk.hubpay.rhaguetest.db.wallet;

import org.junit.jupiter.api.AfterEach;
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

@ExtendWith(SpringExtension.class)
@DataJpaTest
public class WalletRepositoryTest {

    @Autowired
    WalletRepository repository;


    @AfterEach
    public void tearDown(){
        repository.deleteAll();
    }

    @Test
    public void testFindAll(){
        repository.save(new Wallet());
        repository.save(new Wallet());

        List<Wallet> wallets = repository.findAll();
        assertEquals(2, wallets.size());
    }

    @Test
    public void testFindById(){
        Wallet wallet = repository.save(new Wallet());
        repository.save(new Wallet());

        UUID id = wallet.getId();
        Optional<Wallet> byId = repository.findById(id);
        assertTrue(byId.isPresent());

        Wallet test = byId.get();
        assertEquals(id, test.getId());
        assertEquals(BigDecimal.ZERO, test.getBalance());
    }
}
