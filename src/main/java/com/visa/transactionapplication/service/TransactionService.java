package com.visa.transactionapplication.service;

import com.visa.transactionapplication.dto.request.TransactionRequest;
import com.visa.transactionapplication.dto.response.TransactionResponse;
import com.visa.transactionapplication.entity.Account;
import com.visa.transactionapplication.entity.OperationType;
import com.visa.transactionapplication.entity.Transaction;
import com.visa.transactionapplication.exception.ResourceNotFoundException;
import com.visa.transactionapplication.repository.AccountRepository;
import com.visa.transactionapplication.repository.OperationTypeRepository;
import com.visa.transactionapplication.repository.TransactionRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final OperationTypeRepository operationTypeRepository;

    public TransactionService(
            TransactionRepository transactionRepository,
            AccountRepository accountRepository,
            OperationTypeRepository operationTypeRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.operationTypeRepository = operationTypeRepository;
    }

    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request) {
        Account account = accountRepository
                .findById(request.accountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account", request.accountId()));

        OperationType operationType = operationTypeRepository
                .findById(request.operationTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("OperationType", request.operationTypeId()));

        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setOperationType(operationType);
        transaction.setAmount(calculateAmount(operationType, request.amount()));
        transaction.setBalance(calculateBalance(operationType, request.amount(), request.accountId()));
        transaction.setEventDate(Instant.now());

        Transaction saved = transactionRepository.save(transaction);
        return toResponse(saved);
    }

    private BigDecimal calculateAmount(OperationType operationType, BigDecimal amount) {
        return operationType.isRequiresNegative() ? amount.negate() : amount.abs();
    }

    private BigDecimal calculateBalance(OperationType operationType, BigDecimal amount, Long accountId) {
        boolean isDebit = operationType.isRequiresNegative();
        if (isDebit) {
            return amount.negate();
        }
        // discharge the debits
        List<Transaction> transactionList = transactionRepository.findAllPendingTransactions(accountId);
        BigDecimal creditAmount = amount;
        for (Transaction transaction : transactionList) {
            BigDecimal balance = transaction.getBalance();
            if (creditAmount.compareTo(BigDecimal.ZERO) == 0) {
                break;
            }
            BigDecimal dischargeAmount = creditAmount.min(balance.negate());
            creditAmount = creditAmount.subtract(dischargeAmount);
            balance = balance.add(dischargeAmount);
            transaction.setBalance(balance);
            transactionRepository.save(transaction);
        }
        return creditAmount;
    }

    private TransactionResponse toResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getTransactionId(),
                transaction.getAccount().getAccountId(),
                transaction.getOperationType().getOperationTypeId(),
                transaction.getAmount(),
                transaction.getEventDate());
    }
}
