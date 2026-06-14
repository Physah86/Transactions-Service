package com.visa.transactionapplication.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.visa.transactionapplication.dto.request.AccountRequest;
import com.visa.transactionapplication.dto.request.TransactionRequest;
import com.visa.transactionapplication.repository.AccountRepository;
import com.visa.transactionapplication.repository.TransactionRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
class TransactionControllerIT {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        transactionRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @Test
    void createTransaction_whenDebitType_returns201WithNegativeAmount() throws Exception {
        Long accountId = createTestAccount("12345678900");
        TransactionRequest request = new TransactionRequest(accountId, 1L, new BigDecimal("50.00"));

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transaction_id").isNumber())
                .andExpect(jsonPath("$.account_id").value(accountId))
                .andExpect(jsonPath("$.operation_type_id").value(1))
                .andExpect(jsonPath("$.amount").value(-50.00))
                .andExpect(jsonPath("$.event_date").exists());
    }

    @Test
    void createTransaction_whenCreditType_returns201WithPositiveAmount() throws Exception {
        Long accountId = createTestAccount("98765432100");
        TransactionRequest request = new TransactionRequest(accountId, 4L, new BigDecimal("60.00"));

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(60.00));
    }

    @Test
    void createTransaction_whenWithdrawalType_returns201WithNegativeAmount() throws Exception {
        Long accountId = createTestAccount("11122233344");
        TransactionRequest request = new TransactionRequest(accountId, 3L, new BigDecimal("25.00"));

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(-25.00));
    }

    @Test
    void createTransaction_whenAccountNotFound_returns404() throws Exception {
        TransactionRequest request = new TransactionRequest(999L, 1L, new BigDecimal("50.00"));

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(containsString("999")));
    }

    @Test
    void createTransaction_whenOperationTypeNotFound_returns404() throws Exception {
        Long accountId = createTestAccount("55566677788");
        TransactionRequest request = new TransactionRequest(accountId, 99L, new BigDecimal("50.00"));

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void createTransaction_whenMissingAllFields_returns400WithAllFieldErrors() throws Exception {
        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.accountId").exists())
                .andExpect(jsonPath("$.errors.operationTypeId").exists())
                .andExpect(jsonPath("$.errors.amount").exists());
    }

    @Test
    void createTransaction_whenNegativeAmount_returns400() throws Exception {
        Long accountId = createTestAccount("44455566677");
        TransactionRequest request = new TransactionRequest(accountId, 1L, new BigDecimal("-50.00"));

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.amount").exists());
    }

    private Long createTestAccount(String documentNumber) throws Exception {
        AccountRequest request = new AccountRequest(documentNumber);
        String body = mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(body).get("account_id").asLong();
    }
}
