package com.visa.transactionapplication.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private OperationTypeRepository operationTypeRepository;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void createTransaction_whenDebitOperationType_savesWithNegativeAmount() {
        // Given
        TransactionRequest request = new TransactionRequest(1L, 1L, new BigDecimal("50.00"));
        Account account = buildAccount(1L, "12345678900");
        OperationType debitType = buildOperationType(1L, "Normal Purchase", true);
        Transaction saved = buildTransaction(1L, account, debitType, new BigDecimal("-50.00"));

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(operationTypeRepository.findById(1L)).thenReturn(Optional.of(debitType));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(saved);

        // When
        TransactionResponse response = transactionService.createTransaction(request);

        // Then
        assertThat(response.amount()).isEqualByComparingTo(new BigDecimal("-50.00"));

        // Verify what was actually persisted
        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());
        assertThat(captor.getValue().getAmount()).isEqualByComparingTo(new BigDecimal("-50.00"));
    }

    @Test
    void createTransaction_whenCreditOperationType_savesWithPositiveAmount() {
        // Given
        TransactionRequest request = new TransactionRequest(1L, 4L, new BigDecimal("60.00"));
        Account account = buildAccount(1L, "12345678900");
        OperationType creditType = buildOperationType(4L, "Credit Voucher", false);
        Transaction saved = buildTransaction(2L, account, creditType, new BigDecimal("60.00"));

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(operationTypeRepository.findById(4L)).thenReturn(Optional.of(creditType));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(saved);

        // When
        TransactionResponse response = transactionService.createTransaction(request);

        // Then
        assertThat(response.amount()).isEqualByComparingTo(new BigDecimal("60.00"));

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());
        assertThat(captor.getValue().getAmount()).isEqualByComparingTo(new BigDecimal("60.00"));
    }

    @Test
    void createTransaction_whenAccountNotFound_throwsResourceNotFoundException() {
        // Given
        TransactionRequest request = new TransactionRequest(99L, 1L, new BigDecimal("50.00"));
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> transactionService.createTransaction(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void createTransaction_whenOperationTypeNotFound_throwsResourceNotFoundException() {
        // Given
        TransactionRequest request = new TransactionRequest(1L, 99L, new BigDecimal("50.00"));
        Account account = buildAccount(1L, "12345678900");

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(operationTypeRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> transactionService.createTransaction(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void createTransaction_setsEventDateOnServer() {
        // Given
        Instant before = Instant.now();
        TransactionRequest request = new TransactionRequest(1L, 1L, new BigDecimal("50.00"));
        Account account = buildAccount(1L, "12345678900");
        OperationType debitType = buildOperationType(1L, "Normal Purchase", true);
        Transaction saved = buildTransaction(1L, account, debitType, new BigDecimal("-50.00"));

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(operationTypeRepository.findById(1L)).thenReturn(Optional.of(debitType));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(saved);

        // When
        transactionService.createTransaction(request);
        Instant after = Instant.now();

        // Then — event date was set between before and after the call
        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());
        assertThat(captor.getValue().getEventDate()).isAfterOrEqualTo(before).isBeforeOrEqualTo(after);
    }

    private Account buildAccount(Long id, String documentNumber) {
        Account account = new Account();
        account.setAccountId(id);
        account.setDocumentNumber(documentNumber);
        return account;
    }

    private OperationType buildOperationType(Long id, String description, boolean requiresNegative) {
        OperationType type = new OperationType();
        type.setOperationTypeId(id);
        type.setDescription(description);
        type.setRequiresNegative(requiresNegative);
        return type;
    }

    private Transaction buildTransaction(Long id, Account account, OperationType operationType, BigDecimal amount) {
        Transaction transaction = new Transaction();
        transaction.setTransactionId(id);
        transaction.setAccount(account);
        transaction.setOperationType(operationType);
        transaction.setAmount(amount);
        transaction.setEventDate(Instant.now());
        return transaction;
    }
}
