package com.visa.transactionapplication.repository;

import com.visa.transactionapplication.entity.Account;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    boolean existsByDocumentNumber(String documentNumber);

    Optional<Account> findByDocumentNumber(String documentNumber);
}
