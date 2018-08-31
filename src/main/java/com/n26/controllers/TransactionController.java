package com.n26.controllers;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.n26.entities.Statistic;
import com.n26.entities.Transaction;
import com.n26.services.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @PostMapping(value = "/transactions", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> addTransaction(@Valid @RequestBody Transaction transaction, Errors errors){
        if(errors.hasErrors()){
            return new ResponseEntity<>("Validation Error", HttpStatus.BAD_REQUEST);
        }

        transactionService.addTransaction(transaction);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @DeleteMapping("/transactions")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTransactions(){
        transactionService.deleteTransactions();
    }

    @GetMapping("/statistics")
    public ResponseEntity<Statistic> getStatistics(){
        Statistic statistic = transactionService.runStatistics();
        return ResponseEntity.ok(statistic);
    }

    @ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY, reason = "transaction could not be parsed.")
    @ExceptionHandler({UnrecognizedPropertyException.class, InvalidFormatException.class})
    public void handleMessageConversionException() {

    }

}
