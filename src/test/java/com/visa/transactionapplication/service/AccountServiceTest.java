package com.visa.transactionapplication.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.visa.transactionapplication.dto.request.AccountRequest;
import com.visa.transactionapplication.dto.response.AccountResponse;
import com.visa.transactionapplication.entity.Account;
import com.visa.transactionapplication.exception.DocumentAlreadyExistsException;
import com.visa.transactionapplication.exception.ResourceNotFoundException;
import com.visa.transactionapplication.repository.AccountRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    void createAccount_whenValidRequest_returnsAccountResponse() {
        // Given
        AccountRequest request = new AccountRequest("12345678900");
        Account savedAccount = buildAccount(1L, "12345678900");

        when(accountRepository.existsByDocumentNumber("12345678900")).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenReturn(savedAccount);

        // When
        AccountResponse response = accountService.createAccount(request);

        // Then
        assertThat(response.accountId()).isEqualTo(1L);
        assertThat(response.documentNumber()).isEqualTo("12345678900");
        verify(accountRepository).existsByDocumentNumber("12345678900");
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void createAccount_whenDuplicateDocumentNumber_throwsDocumentAlreadyExistsException() {
        // Given
        AccountRequest request = new AccountRequest("12345678900");
        when(accountRepository.existsByDocumentNumber("12345678900")).thenReturn(true);

        // When / Then
        assertThatThrownBy(() -> accountService.createAccount(request))
                .isInstanceOf(DocumentAlreadyExistsException.class)
                .hasMessageContaining("12345678900");

        verify(accountRepository, never()).save(any());
    }

    @Test
    void getAccountById_whenAccountExists_returnsAccountResponse() {
        // Given
        Account account = buildAccount(1L, "12345678900");
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        // When
        AccountResponse response = accountService.getAccountById(1L);

        // Then
        assertThat(response.accountId()).isEqualTo(1L);
        assertThat(response.documentNumber()).isEqualTo("12345678900");
    }

    @Test
    void getAccountById_whenAccountNotFound_throwsResourceNotFoundException() {
        // Given
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> accountService.getAccountById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    private Account buildAccount(Long id, String documentNumber) {
        Account account = new Account();
        account.setAccountId(id);
        account.setDocumentNumber(documentNumber);
        return account;
    }
}
