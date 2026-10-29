package com.indra.jpa.transactions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import com.indra.jpa.transactions.exception.InsufficientFundsException;
import com.indra.jpa.transactions.model.Account;
import com.indra.jpa.transactions.repository.AccountRepository;
import com.indra.jpa.transactions.repository.TransferEventRepository;
import com.indra.jpa.transactions.service.TransferService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
		properties = "spring.jpa.properties.hibernate.hbm2ddl.halt_on_error=true")
class TransactionsApplicationTests {

	@Autowired
	private AccountRepository accountRepository;

	@Autowired
	private TransferEventRepository transferEventRepository;

	@Autowired
	private TransferService transferService;

	@Autowired
	private DataSource dataSource;

	private Account source;
	private Account destination;

	@BeforeEach
	void setUp() {
		transferEventRepository.deleteAll();
		accountRepository.deleteAll();
		source = createAccount("Alice", "100.00");
		destination = createAccount("Bob", "50.00");
	}

	@Test
	@DisplayName("Revierte los saldos de ambas cuentas cuando no hay fondos suficientes")
	void insufficientFundsRollsBackBothBalances() {
		assertThatThrownBy(() -> transferService.transfer(source.getId(), destination.getId(),
				new BigDecimal("150.00")))
				.isInstanceOf(InsufficientFundsException.class);

		assertBalance(source.getId(), "100.00");
		assertBalance(destination.getId(), "50.00");
	}

	@Test
	@DisplayName("Conserva el evento de auditoría aunque la transferencia se revierta")
	void auditSurvivesTransferRollback() {
		assertThatThrownBy(() -> transferService.transfer(source.getId(), destination.getId(),
				new BigDecimal("150.00")))
				.isInstanceOf(InsufficientFundsException.class);

		assertThat(transferEventRepository.findAll()).singleElement().satisfies(event -> {
			assertThat(event.getFromAccountId()).isEqualTo(source.getId());
			assertThat(event.getToAccountId()).isEqualTo(destination.getId());
			assertThat(event.getAmount()).isEqualByComparingTo("150.00");
			assertThat(event.getCreatedAt()).isNotNull();
		});
		assertBalance(source.getId(), "100.00");
		assertBalance(destination.getId(), "50.00");
	}

	@Test
	@DisplayName("Actualiza los saldos de ambas cuentas al completar una transferencia")
	void successfulTransferUpdatesBothBalances() throws InsufficientFundsException {
		transferService.transfer(source.getId(), destination.getId(), new BigDecimal("25.10"));

		assertBalance(source.getId(), "74.90");
		assertBalance(destination.getId(), "75.10");
		assertThat(transferEventRepository.count()).isEqualTo(1);
	}

	@Test
	@DisplayName("NESTED pierde el log al revertir la transaccion externa, a diferencia de REQUIRES_NEW")
	void nestedAuditIsRolledBackWithOuterTransaction() {
		JdbcTemplate jdbc = new JdbcTemplate(dataSource);
		DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
		TransactionTemplate transferTransaction = new TransactionTemplate(transactionManager);
		TransactionTemplate auditTransaction = new TransactionTemplate(transactionManager);
		auditTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_NESTED);

		transferTransaction.executeWithoutResult(transferStatus -> {
			auditTransaction.executeWithoutResult(auditStatus -> {
				assertThat(auditStatus.hasSavepoint()).isTrue();
				jdbc.update("""
						INSERT INTO transfer_events (from_account_id, to_account_id, amount, created_at)
						VALUES (?, ?, ?, ?)
						""", source.getId(), destination.getId(), new BigDecimal("150.00"),
						Timestamp.from(Instant.now()));
			});

			assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM transfer_events", Long.class)).isEqualTo(1L);
			jdbc.update("UPDATE accounts SET balance = balance - ? WHERE id = ?",
					new BigDecimal("150.00"), source.getId());
			transferStatus.setRollbackOnly();
		});

		assertThat(transferEventRepository.count()).isZero();
		assertBalance(source.getId(), "100.00");
		assertBalance(destination.getId(), "50.00");
	}

	private Account createAccount(String owner, String balance) {
		Account account = new Account();
		account.setOwner(owner);
		account.setBalance(new BigDecimal(balance));
		return accountRepository.saveAndFlush(account);
	}

	private void assertBalance(Long accountId, String expectedBalance) {
		assertThat(accountRepository.findById(accountId).orElseThrow().getBalance())
				.isEqualByComparingTo(expectedBalance);
	}
}
