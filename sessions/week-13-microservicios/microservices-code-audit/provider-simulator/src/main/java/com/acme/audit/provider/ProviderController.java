package com.acme.audit.provider;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

@RestController
@Validated
@RequestMapping("/provider")
public class ProviderController {
	private static final Logger log = LogManager.getLogger(ProviderController.class);
	private final ProviderSimulatorService service;
	
	public ProviderController(ProviderSimulatorService service) {
		this.service = service;
	}

	@PostMapping("/charges")
	public ResponseEntity<ChargeResponse> charge(@RequestHeader("Idempotency-Key") @NotBlank @Size(max = 150) String key,
			@Valid @RequestBody ChargeRequest request) {
		
		var result = service.charge(key, request.orderId(), request.amount(), request.currency());
		log.info("PROVIDER | idTx=[{}] | event=CHARGE_PROCESSED | orderId={} | key={} | providerTransactionId={} | replayed={}",
				org.apache.logging.log4j.ThreadContext.get("idTx"), request.orderId(),  key, result.providerTransactionId(), result.replayed());
		// The business effect is committed before the intentionally slow response.
		service.delayResponse();
		return ResponseEntity.ok(new ChargeResponse(result.providerTransactionId(), result.orderId(), result.status(),
				result.replayed()));
	}

	@GetMapping("/metrics")
	public ProviderSimulatorService.Metrics metrics() {
		return service.metrics();
	}

	@GetMapping("/charges")
	public java.util.List<ProviderSimulatorService.ChargeView> charges() {
		return service.charges();
	}

	@DeleteMapping("/metrics")
	public ResponseEntity<Void> reset() {
		service.reset();
		return ResponseEntity.noContent().build();
	}

    @ExceptionHandler(ProviderSimulatorService.IdempotencyKeyConflictException.class)
    public ResponseEntity<ProblemResponse> idempotencyConflict(ProviderSimulatorService.IdempotencyKeyConflictException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ProblemResponse("IDEMPOTENCY_KEY_REUSED", exception.getMessage()));
    }

    public record ProblemResponse(String code, String message) { }

	public record ChargeRequest(@NotBlank @Size(max = 80) String orderId, @NotNull @Positive BigDecimal amount,
			@NotBlank @Pattern(regexp = "[A-Z]{3}") String currency) {
	}

	public record ChargeResponse(String providerTransactionId, String orderId, String status, boolean replayed) {
	}
}
