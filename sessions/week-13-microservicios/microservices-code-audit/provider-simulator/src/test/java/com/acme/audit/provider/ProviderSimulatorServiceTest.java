package com.acme.audit.provider;

import static org.assertj.core.api.Assertions.assertThat;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ProviderSimulatorServiceTest {
    @Test
    void repeatedRequestsWithSameKeyCreateOneChargeButCountEveryRequest() {
        ProviderSimulatorService service = new ProviderSimulatorService(0);
        var first = service.charge("ORDER-1-PAYMENT", "ORDER-1", new BigDecimal("100.00"), "COP");
        var replay = service.charge("ORDER-1-PAYMENT", "ORDER-1", new BigDecimal("100.00"), "COP");
        assertThat(replay.providerTransactionId()).isEqualTo(first.providerTransactionId());
        assertThat(replay.replayed()).isTrue();
        assertThat(service.metrics().totalRequests()).isEqualTo(2);
        assertThat(service.metrics().uniqueCharges()).isEqualTo(1);
    }

    @Test
    void sameIdempotencyKeyWithDifferentPayloadIsRejected() {
        ProviderSimulatorService service = new ProviderSimulatorService(0);
        service.charge("ORDER-1-PAYMENT", "ORDER-1", new BigDecimal("100.00"), "COP");
        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                service.charge("ORDER-1-PAYMENT", "ORDER-1", new BigDecimal("200.00"), "COP"))
                .isInstanceOf(ProviderSimulatorService.IdempotencyKeyConflictException.class);
    }
}
