package com.indra.jpa.transactions.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.indra.jpa.transactions.model.TransferEvent;
import com.indra.jpa.transactions.repository.TransferEventRepository;

@Service 
public class TransferAuditService {

    private final TransferEventRepository transferEventRepository;

    public TransferAuditService(TransferEventRepository transferEventRepository) {
        this.transferEventRepository = transferEventRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(TransferEvent event) {
        transferEventRepository.save(event);
    }
}
