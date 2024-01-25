package uk.hubpay.rhaguetest.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.util.Streams;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import uk.hubpay.rhaguetest.db.wallet.Wallet;
import uk.hubpay.rhaguetest.db.wallet.WalletRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    public void testCredit_happyPath() throws Exception {
        List<Wallet> all = walletRepository.findAll();
        Wallet wallet = all.get(0);
        WalletUpdate walletUpdate = new WalletUpdate(wallet.getId(), BigDecimal.valueOf(1000));

        this.mockMvc.perform(post("/wallet/credit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(walletUpdate)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(containsString("Credit Successful")));
    }

    @Test
    public void testDebit_happyPath() throws Exception {
        List<Wallet> all = walletRepository.findAll();
        Wallet wallet = all.get(0);
        WalletUpdate walletUpdate = new WalletUpdate(wallet.getId(), BigDecimal.valueOf(1000));

        this.mockMvc.perform(post("/wallet/debit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(walletUpdate)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(containsString("Debit Successful")));
    }

    @Test
    public void testBalance_happyPath() throws Exception {
        List<Wallet> all = walletRepository.findAll();
        Wallet wallet = all.get(0);
        WalletDto walletDto = new WalletDto(wallet.getBalance(), wallet.getUpdated(), wallet.getCreated());
        this.mockMvc.perform(get("/wallet/{uuid}/balance", wallet.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(objectMapper.writeValueAsString(walletDto)));
    }

    @Test
    public void testTransactions_happyPath() throws Exception {
        List<Wallet> all = walletRepository.findAll();
        Wallet wallet = all.get(0);
        MvcResult mvcResult = this.mockMvc.perform(get("/wallet/{uuid}/transactions", wallet.getId())
                        .param("page", "0")
                        .param("size", "5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        JsonNode jsonNode = objectMapper.readTree(mvcResult.getResponse().getContentAsString());
        JsonNode content = jsonNode.get("content");

        List<JsonNode> jsonNodes = Streams.stream(content.elements()).toList();
        assertEquals(5, jsonNodes.size());
    }

    @Test
    public void testConcurrency() throws Exception {
        List<Wallet> all = walletRepository.findAll();
        Wallet wallet = all.get(1);
        WalletUpdate walletUpdate = new WalletUpdate(wallet.getId(), BigDecimal.valueOf(1000));

        this.mockMvc.perform(post("/wallet/credit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(walletUpdate)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(containsString("Credit Successful")));


        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(20);

        WalletUpdate walletUpdate2 = new WalletUpdate(wallet.getId(), BigDecimal.valueOf(100));
        List<ScheduledFuture<?>> tasks = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            ScheduledFuture<?> future = scheduledExecutorService.schedule(() -> {
                try {
                    this.mockMvc.perform(post("/wallet/debit")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(walletUpdate2)));
                } catch (Exception ignore) {
                }
            }, 5, TimeUnit.MILLISECONDS);
            tasks.add(future);
        }
        List<Object> finishedtasks = tasks.stream().map(f -> {
            try {
                return f.get();
            } catch (Exception e) {
                return e;
            }
        }).toList();

        assertEquals(20, finishedtasks.size());

        Wallet updated = walletRepository.findById(wallet.getId()).get();
        assertEquals(BigDecimal.ZERO.compareTo(updated.getBalance()), 0);
    }
}