package com.visa.transactionapplication.service;

import com.visa.transactionapplication.dto.request.AccountRequest;
import com.visa.transactionapplication.dto.response.AccountResponse;
import com.visa.transactionapplication.entity.Account;
import com.visa.transactionapplication.exception.DocumentAlreadyExistsException;
import com.visa.transactionapplication.exception.ResourceNotFoundException;
import com.visa.transactionapplication.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public AccountResponse createAccount(AccountRequest request) {
        if (accountRepository.existsByDocumentNumber(request.documentNumber())) {
            throw new DocumentAlreadyExistsException(request.documentNumber());
        }

        Account account = new Account();
        account.setDocumentNumber(request.documentNumber());

        Account saved = accountRepository.save(account);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccountById(Long accountId) {
        Account account = accountRepository
                .findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", accountId));
        return toResponse(account);
    }

    private AccountResponse toResponse(Account account) {
        return new AccountResponse(account.getAccountId(), account.getDocumentNumber());
    }
}
