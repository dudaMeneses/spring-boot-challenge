package com.n26.controllers;

import com.n26.entities.Statistic;
import com.n26.entities.Transaction;
import com.n26.services.TransactionService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;

@RunWith(SpringRunner.class)
@WebMvcTest(value = TransactionController.class, secure = false)
public class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @Test
    public void addTransaction_whenInvalidJson_shouldThrowUnprocessableEntity() throws Exception {
        MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders.post("/transactions")
                                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                                    .content("{\"test\": \"test\"}"))
                                    .andReturn();
        assertEquals(422, result.getResponse().getStatus());
    }

    @Test
    public void addTransaction_whenInvalidData_shouldThrowBadRequest() throws Exception {
        MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders.post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("Test"))
                .andReturn();
        assertEquals(400, result.getResponse().getStatus());
    }

    @Test
    public void addTransaction_whenInvalidTimestamp_shouldThrowUnprocessableEntity() throws Exception {
        MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders.post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("{\"amount\":\"3.288\",\"timestamp\":\"17-10-1988\"}"))
                .andReturn();
        assertEquals(422, result.getResponse().getStatus());
    }

    @Test
    public void addTransaction_whenHappyPath_shouldReturnStatusCreated() throws Exception {
        doNothing().when(transactionService).addTransaction(isA(Transaction.class));

        MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders.post("/transactions")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("{\"amount\":\"3.288\",\"timestamp\":\"2018-08-31T05:52:52.521Z\"}"))
                .andReturn();
        assertEquals(201, result.getResponse().getStatus());
    }

    @Test
    public void deleteTransactions_whenHappyPath_shouldReturnStatusNoContent() throws Exception {
        doNothing().when(transactionService).deleteTransactions();

        MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders.delete("/transactions")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(""))
                .andReturn();
        assertEquals(204, result.getResponse().getStatus());
    }

    @Test
    public void getStatistics_whenHappyPath_shouldReturnStatisticEntity() throws Exception {
        doReturn(new Statistic.StatisticBuilder().build()).when(transactionService).runStatistics();

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/statistics")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        assertThat(result.getResponse(), allOf(
                hasProperty("status", equalTo(200)),
                hasProperty("contentAsString", equalTo("{\"sum\":\"0\",\"avg\":\"0\",\"max\":\"0\",\"min\":\"0\",\"count\":0}"))
        ));
    }
}