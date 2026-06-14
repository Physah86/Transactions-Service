package com.visa.transactionapplication.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.visa.transactionapplication.dto.request.AccountRequest;
import com.visa.transactionapplication.repository.AccountRepository;
import com.visa.transactionapplication.repository.TransactionRepository;
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
class AccountControllerIT {

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
    void createAccount_whenValidRequest_returns201WithBody() throws Exception {
        AccountRequest request = new AccountRequest("12345678900");

        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.account_id").isNumber())
                .andExpect(jsonPath("$.document_number").value("12345678900"));
    }

    @Test
    void createAccount_whenDuplicateDocumentNumber_returns409() throws Exception {
        AccountRequest request = new AccountRequest("12345678900");
        String body = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value(containsString("12345678900")));
    }

    @Test
    void createAccount_whenDocumentNumberMissing_returns400WithFieldError() throws Exception {
        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.documentNumber").exists());
    }

    @Test
    void createAccount_whenDocumentNumberBlank_returns400WithFieldError() throws Exception {
        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"document_number\": \"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.documentNumber").exists());
    }

    @Test
    void getAccount_whenAccountExists_returns200WithBody() throws Exception {
        AccountRequest request = new AccountRequest("12345678900");
        String createBody = mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long accountId = objectMapper.readTree(createBody).get("account_id").asLong();

        mockMvc.perform(get("/accounts/{id}", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.account_id").value(accountId))
                .andExpect(jsonPath("$.document_number").value("12345678900"));
    }

    @Test
    void getAccount_whenAccountNotFound_returns404() throws Exception {
        mockMvc.perform(get("/accounts/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(containsString("999")));
    }
}
