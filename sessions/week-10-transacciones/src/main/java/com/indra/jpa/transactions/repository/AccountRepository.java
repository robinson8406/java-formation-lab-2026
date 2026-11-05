package com.indra.jpa.transactions.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import com.indra.jpa.transactions.model.Account;
public interface AccountRepository extends JpaRepository<Account, Long> {

}
