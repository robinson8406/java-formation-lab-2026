package com.indra.jpa.transactions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.indra.jpa.transactions.exception.InsufficientFundsException;
import com.indra.jpa.transactions.model.Account;
import com.indra.jpa.transactions.repository.AccountRepository;
import com.indra.jpa.transactions.repository.TransferEventRepository;
import com.indra.jpa.transactions.service.TransferAuditService;
import com.indra.jpa.transactions.service.TransferService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
		properties = "spring.jpa.properties.hibernate.hbm2ddl.halt_on_error=true")
class TransactionsApplicationTests {

	private final AccountRepository accountRepository;
	private final TransferEventRepository transferEventRepository;
	private final TransferService transferService;
	private final TransferAuditService transferAuditService;

	private Account source;
	private Account destination;

	@Autowired
	public TransactionsApplicationTests(AccountRepository accountRepository,
			TransferEventRepository transferEventRepository,
			TransferService transferService,
			TransferAuditService transferAuditService) {
		this.accountRepository = accountRepository;
		this.transferEventRepository = transferEventRepository;
		this.transferService = transferService;
		this.transferAuditService = transferAuditService;
	}

	@BeforeEach
	void setUp() {
		transferEventRepository.deleteAll();
		accountRepository.deleteAll();
		source = createAccount("Alice", "100.00");
		destination = createAccount("Bob", "50.00");
	}

	@Test
	@DisplayName("Escenario 1: Fondos insuficientes deben revertir la transacción sin alterar ningún saldo")
	void insufficientFundsRollsBackBothBalances() {
		BigDecimal amountToTransfer = new BigDecimal("150.00");

		assertThrows(InsufficientFundsException.class, () ->
				transferService.transfer(source.getId(), destination.getId(), amountToTransfer)
		);

		assertBalance(source.getId(), "100.00");
		assertBalance(destination.getId(), "50.00");
	}

	@Test
	@DisplayName("Escenario 2: El log de auditoría debe persistir mediante transacción independiente aunque la transferencia falle")
	void auditSurvivesTransferRollback() {
		BigDecimal amountToTransfer = new BigDecimal("150.00");

		assertThrows(InsufficientFundsException.class, () ->
				transferService.transfer(source.getId(), destination.getId(), amountToTransfer)
		);

		assertThat(transferEventRepository.count()).isEqualTo(1L);
		var event = transferEventRepository.findAll().get(0);
		assertThat(event.getFromAccountId()).isEqualTo(source.getId());
		assertThat(event.getToAccountId()).isEqualTo(destination.getId());
		assertThat(event.getAmount()).isEqualByComparingTo(amountToTransfer);
		assertThat(event.getCreatedAt()).isNotNull();
	}

	@Test
	@DisplayName("Bonus: Transferencia exitosa actualiza saldos correctamente y registra auditoría")
	void successfulTransferUpdatesBothBalancesAndLogsAudit() throws InsufficientFundsException {
		BigDecimal amountToTransfer = new BigDecimal("30.00");

		transferService.transfer(source.getId(), destination.getId(), amountToTransfer);

		assertBalance(source.getId(), "70.00");
		assertBalance(destination.getId(), "80.00");

		assertThat(transferEventRepository.count()).isEqualTo(1L);
		var event = transferEventRepository.findAll().get(0);
		assertThat(event.getFromAccountId()).isEqualTo(source.getId());
		assertThat(event.getToAccountId()).isEqualTo(destination.getId());
		assertThat(event.getAmount()).isEqualByComparingTo(amountToTransfer);
	}

	@Test
	@DisplayName("Debe rechazar transferencia cuando el monto es nulo, cero o negativo")
	void shouldRejectTransferWhenAmountIsZeroOrNegative() {
		assertThrows(IllegalArgumentException.class, () ->
				transferService.transfer(source.getId(), destination.getId(), null)
		);
		assertThrows(IllegalArgumentException.class, () ->
				transferService.transfer(source.getId(), destination.getId(), BigDecimal.ZERO)
		);
		assertThrows(IllegalArgumentException.class, () ->
				transferService.transfer(source.getId(), destination.getId(), new BigDecimal("-10.00"))
		);

		assertBalance(source.getId(), "100.00");
		assertBalance(destination.getId(), "50.00");
	}

	@Test
	@DisplayName("Debe rechazar transferencia cuando las cuentas son idénticas o nulas")
	void shouldRejectTransferWhenAccountsAreSameOrNull() {
		BigDecimal amountToTransfer = new BigDecimal("20.00");

		assertThrows(IllegalArgumentException.class, () ->
				transferService.transfer(null, destination.getId(), amountToTransfer)
		);
		assertThrows(IllegalArgumentException.class, () ->
				transferService.transfer(source.getId(), null, amountToTransfer)
		);
		assertThrows(IllegalArgumentException.class, () ->
				transferService.transfer(source.getId(), source.getId(), amountToTransfer)
		);
	}

	@Test
	@DisplayName("Debe rechazar transferencia cuando la cuenta origen o destino no existe")
	void shouldRejectTransferWhenAccountDoesNotExist() {
		BigDecimal amountToTransfer = new BigDecimal("20.00");

		assertThrows(IllegalArgumentException.class, () ->
				transferService.transfer(9999L, destination.getId(), amountToTransfer)
		);
		assertThrows(IllegalArgumentException.class, () ->
				transferService.transfer(source.getId(), 9999L, amountToTransfer)
		);
	}

	@Test
	@DisplayName("Debe rechazar evento de auditoría nulo")
	void shouldRejectNullEventWhenLoggingAudit() {
		assertThrows(IllegalArgumentException.class, () ->
				transferAuditService.log(null)
		);
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
