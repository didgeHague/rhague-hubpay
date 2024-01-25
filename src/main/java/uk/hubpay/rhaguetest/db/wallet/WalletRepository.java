package uk.hubpay.rhaguetest.db.wallet;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WalletRepository extends CrudRepository<Wallet, UUID> {
    Optional<Wallet> findById(UUID uuid);

    List<Wallet> findAll();
}
