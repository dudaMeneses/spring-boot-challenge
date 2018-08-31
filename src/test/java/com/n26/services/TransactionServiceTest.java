package com.n26.services;

import com.n26.entities.Statistic;
import com.n26.entities.Transaction;
import com.n26.exceptions.FutureTransactionException;
import com.n26.exceptions.OldMessageException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TransactionServiceTest {

    @Autowired
    private TransactionService transactionService;

    @Before
    public void init(){
        transactionService.init();
    }

    @Test
    public void addTransaction_whenAddFirstTransaction_transactionListShouldHaveOneItem(){
        Transaction transaction = new Transaction(new BigDecimal(123.21), LocalDateTime.now(Clock.systemUTC()));

        transactionService.addTransaction(transaction);

        assertThat(transactionService.getTransactionsList(), allOf(hasItem(transaction), iterableWithSize(1)));
    }

    @Test
    public void addTransaction_whenAddFourTransactions_transactionListShouldHaveFourItems(){
        Transaction transaction1 = new Transaction(new BigDecimal(123.21), LocalDateTime.now(Clock.systemUTC()));
        Transaction transaction2 = new Transaction(new BigDecimal(123.21), LocalDateTime.now(Clock.systemUTC()));
        Transaction transaction3 = new Transaction(new BigDecimal(123.21), LocalDateTime.now(Clock.systemUTC()));
        Transaction transaction4 = new Transaction(new BigDecimal(123.21), LocalDateTime.now(Clock.systemUTC()));

        transactionService.addTransaction(transaction1);
        transactionService.addTransaction(transaction2);
        transactionService.addTransaction(transaction3);
        transactionService.addTransaction(transaction4);

        assertThat(transactionService.getTransactionsList(), allOf(
                hasItems(transaction1, transaction2, transaction3, transaction4),
                iterableWithSize(4)
        ));
    }

    @Test(expected = OldMessageException.class)
    public void addTransaction_whenTransactionOlderThan60Seconds_shouldThrowOldMessageException(){
        Transaction transaction = new Transaction(new BigDecimal(123.21), LocalDateTime.now(Clock.systemUTC()).minusSeconds(61));

        transactionService.addTransaction(transaction);
    }

    @Test(expected = FutureTransactionException.class)
    public void addTransaction_whenTransactionDateIsInFuture_shouldThrowFutureTransactionException(){
        Transaction transaction = new Transaction(new BigDecimal(123.21), LocalDateTime.now(Clock.systemUTC()).plusMinutes(2));

        transactionService.addTransaction(transaction);
    }

    @Test
    public void runStatistics_whenNoItemsToEvaluate_shouldGiveEmptyResult(){
        Statistic statistic = transactionService.runStatistics();

        assertThat(statistic, allOf(
            hasProperty("sum", equalTo(new BigDecimal(0).setScale(2, BigDecimal.ROUND_HALF_UP))),
            hasProperty("avg", equalTo(new BigDecimal(0).setScale(2, BigDecimal.ROUND_HALF_UP))),
            hasProperty("max", equalTo(new BigDecimal(0).setScale(2, BigDecimal.ROUND_HALF_UP))),
            hasProperty("min", equalTo(new BigDecimal(0).setScale(2, BigDecimal.ROUND_HALF_UP))),
            hasProperty("count", equalTo(0L))
        ));
    }

    @Test
    public void runStatistics_whenOneItemToEvaluate_shouldReturnThatItemInformation(){
        transactionService.addTransaction(new Transaction(new BigDecimal(123.21), LocalDateTime.now(Clock.systemUTC())));

        Statistic statistic = transactionService.runStatistics();

        assertThat(statistic, allOf(
                hasProperty("sum", equalTo(new BigDecimal(123.21).setScale(2, BigDecimal.ROUND_HALF_UP))),
                hasProperty("avg", equalTo(new BigDecimal(123.21).setScale(2, BigDecimal.ROUND_HALF_UP))),
                hasProperty("max", equalTo(new BigDecimal(123.21).setScale(2, BigDecimal.ROUND_HALF_UP))),
                hasProperty("min", equalTo(new BigDecimal(123.21).setScale(2, BigDecimal.ROUND_HALF_UP))),
                hasProperty("count", equalTo(1L))
        ));
    }

    @Test
    public void runStatistics_whenThreeItemsToEvaluate_shouldReturnCorrectStatistics(){
        transactionService.addTransaction(new Transaction(new BigDecimal(50.00), LocalDateTime.now(Clock.systemUTC())));
        transactionService.addTransaction(new Transaction(new BigDecimal(100.50), LocalDateTime.now(Clock.systemUTC())));
        transactionService.addTransaction(new Transaction(new BigDecimal(12.21), LocalDateTime.now(Clock.systemUTC())));

        Statistic statistic = transactionService.runStatistics();

        assertThat(statistic, allOf(
                hasProperty("sum", equalTo(new BigDecimal(162.71).setScale(2, BigDecimal.ROUND_HALF_UP))),
                hasProperty("avg", equalTo(new BigDecimal(54.24).setScale(2, BigDecimal.ROUND_HALF_UP))),
                hasProperty("max", equalTo(new BigDecimal(100.50).setScale(2, BigDecimal.ROUND_HALF_UP))),
                hasProperty("min", equalTo(new BigDecimal(12.21).setScale(2, BigDecimal.ROUND_HALF_UP))),
                hasProperty("count", equalTo(3L))
        ));
    }

    @Test
    public void runStatistics_whenThereIsAnTransactionOlderThan60Seconds_shouldReturnOnlyStatisticsFromLastMinuteTransactions(){
        transactionService.getTransactionsList().add(new Transaction(new BigDecimal(50.00), LocalDateTime.now(Clock.systemUTC()).minusSeconds(80)));

        transactionService.addTransaction(new Transaction(new BigDecimal(115.80), LocalDateTime.now(Clock.systemUTC())));
        transactionService.addTransaction(new Transaction(new BigDecimal(12.21), LocalDateTime.now(Clock.systemUTC())));

        Statistic statistic = transactionService.runStatistics();

        assertThat(statistic, allOf(
                hasProperty("sum", equalTo(new BigDecimal(128.01).setScale(2, BigDecimal.ROUND_HALF_UP))),
                hasProperty("avg", equalTo(new BigDecimal(64.00).setScale(2, BigDecimal.ROUND_HALF_UP))),
                hasProperty("max", equalTo(new BigDecimal(115.80).setScale(2, BigDecimal.ROUND_HALF_UP))),
                hasProperty("min", equalTo(new BigDecimal(12.21).setScale(2, BigDecimal.ROUND_HALF_UP))),
                hasProperty("count", equalTo(2L))
        ));
    }

    @Test
    public void runStatistics_whenThereIsAnTransaction1MinuteAhead_shouldReturnOnlyStatisticsFromLastMinuteTransactions(){
        transactionService.getTransactionsList().add(new Transaction(new BigDecimal(50.00), LocalDateTime.now(Clock.systemUTC()).plusSeconds(80)));

        transactionService.addTransaction(new Transaction(new BigDecimal(115.80), LocalDateTime.now(Clock.systemUTC())));
        transactionService.addTransaction(new Transaction(new BigDecimal(12.21), LocalDateTime.now(Clock.systemUTC())));

        Statistic statistic = transactionService.runStatistics();

        assertThat(statistic, allOf(
                hasProperty("sum", equalTo(new BigDecimal(128.01).setScale(2, BigDecimal.ROUND_HALF_UP))),
                hasProperty("avg", equalTo(new BigDecimal(64.00).setScale(2, BigDecimal.ROUND_HALF_UP))),
                hasProperty("max", equalTo(new BigDecimal(115.80).setScale(2, BigDecimal.ROUND_HALF_UP))),
                hasProperty("min", equalTo(new BigDecimal(12.21).setScale(2, BigDecimal.ROUND_HALF_UP))),
                hasProperty("count", equalTo(2L))
        ));
    }

    @Test
    public void deleteTransactions_whenHappyPath_shouldDeleteTransactionsList(){
        Transaction transaction1 = new Transaction(new BigDecimal(123.21), LocalDateTime.now(Clock.systemUTC()));
        Transaction transaction2 = new Transaction(new BigDecimal(123.21), LocalDateTime.now(Clock.systemUTC()));
        Transaction transaction3 = new Transaction(new BigDecimal(123.21), LocalDateTime.now(Clock.systemUTC()));
        Transaction transaction4 = new Transaction(new BigDecimal(123.21), LocalDateTime.now(Clock.systemUTC()));

        transactionService.addTransaction(transaction1);
        transactionService.addTransaction(transaction2);
        transactionService.addTransaction(transaction3);
        transactionService.addTransaction(transaction4);

        transactionService.deleteTransactions();

        assertEquals(0, transactionService.getTransactionsList().size());

    }
}