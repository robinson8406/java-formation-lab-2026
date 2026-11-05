package com.indra.jpa.transactions.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.indra.jpa.transactions.model.TransferEvent;

public interface TransferEventRepository extends JpaRepository<TransferEvent, Long> {
}