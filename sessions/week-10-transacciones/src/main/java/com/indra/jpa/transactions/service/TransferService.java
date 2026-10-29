package com.indra.jpa.transactions.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.indra.jpa.transactions.exception.InsufficientFundsException;
import com.indra.jpa.transactions.model.Account;
import com.indra.jpa.transactions.model.TransferEvent;
import com.indra.jpa.transactions.repository.AccountRepository;

@Service 
public class TransferService {

    private final AccountRepository accountRepository;
    private final TransferAuditService transferAuditService;

    public TransferService(AccountRepository accountRepository, TransferAuditService transferAuditService) {
        this.accountRepository = accountRepository;
        this.transferAuditService = transferAuditService;
    }

    @Transactional(rollbackFor = InsufficientFundsException.class)
    public void transfer(Long fromAccountId, Long toAccountId, BigDecimal amount)
            throws InsufficientFundsException {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (fromAccountId == null || toAccountId == null || fromAccountId.equals(toAccountId)) {
            throw new IllegalArgumentException("Two different accounts are required");
        }

        Account fromAccount = accountRepository.findById(fromAccountId)
                .orElseThrow(() -> new IllegalArgumentException("Source account not found"));
        Account toAccount = accountRepository.findById(toAccountId)
                .orElseThrow(() -> new IllegalArgumentException("Destination account not found"));

        TransferEvent event = new TransferEvent();
        event.setFromAccountId(fromAccountId);
        event.setToAccountId(toAccountId);
        event.setAmount(amount);
        transferAuditService.log(event);

        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        accountRepository.saveAndFlush(fromAccount);
        if (fromAccount.getBalance().signum() < 0) {
            throw new InsufficientFundsException("Insufficient funds in account " + fromAccountId);
        }

        toAccount.setBalance(toAccount.getBalance().add(amount));
        accountRepository.save(toAccount);
    }

}
