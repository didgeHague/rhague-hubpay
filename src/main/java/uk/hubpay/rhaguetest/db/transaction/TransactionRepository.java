package uk.hubpay.rhaguetest.db.transaction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    List<Transaction> findAll();

    Optional<Transaction> findById(UUID uuid);

    List<Transaction> findByWalletId(UUID walletId);

    Page<Transaction> findByWalletIdOrderByTimestampAsc(UUID walletId, Pageable pageable);
}
