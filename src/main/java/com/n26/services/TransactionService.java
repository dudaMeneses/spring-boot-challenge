package com.n26.services;

import com.n26.entities.Statistic;
import com.n26.entities.Transaction;
import com.n26.exceptions.FutureTransactionException;
import com.n26.exceptions.OldMessageException;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.DoubleStream;

@Component
public class TransactionService {

    private List<Transaction> transactionsList;

    @PostConstruct
    public void init(){
        transactionsList = new ArrayList<>();
    }

    public void addTransaction(Transaction transaction) {
        validateTimestamp(transaction);
        transactionsList.add(transaction);
    }

    public Statistic runStatistics() {
        Supplier<DoubleStream> amountSupplier = () -> transactionsList.stream()
                                                                  .filter(i -> Math.abs(Duration.between(Instant.now(), i.getTimestamp().atZone(ZoneOffset.UTC).toInstant()).getSeconds()) < 60)
                                                                  .map(Transaction::getAmount)
                                                                  .mapToDouble(BigDecimal::doubleValue);

        return new Statistic.StatisticBuilder()
                .sum(amountSupplier.get().sum())
                .avg(amountSupplier.get().average())
                .max(amountSupplier.get().max())
                .min(amountSupplier.get().min())
                .count(amountSupplier.get().count())
                .build();
    }

    public void deleteTransactions() {
        transactionsList.clear();
    }

    private void validateTimestamp(Transaction transaction) {
        if(isBeforeMinimumTimestamp(transaction)){
            throw new OldMessageException();
        }

        if(isAfterMaximumTimestamp(transaction)){
            throw new FutureTransactionException();
        }
    }

    private boolean isAfterMaximumTimestamp(Transaction transaction) {
        return transaction.getTimestamp().atZone(ZoneOffset.UTC).toInstant().toEpochMilli() > Instant.now().toEpochMilli();
    }

    private boolean isBeforeMinimumTimestamp(Transaction transaction) {
        return transaction.getTimestamp().atZone(ZoneOffset.UTC).toInstant().toEpochMilli() < Instant.now().toEpochMilli() - 60000;
    }

    public List<Transaction> getTransactionsList() {
        return transactionsList;
    }

}
