package com.visa.transactionapplication.repository;

import com.visa.transactionapplication.entity.Transaction;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByAccount_AccountId(Long accountId);

    @Query(
            value =
                    "SELECT * from transactions t where t.account_id = :accountId AND t.balance < 0 ORDER BY t.event_date",
            nativeQuery = true)
    List<Transaction> findAllPendingTransactions(@Param("accountId") Long accountId);
}
