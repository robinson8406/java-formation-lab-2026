package com.acme.audit.payment.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.acme.audit.payment.application.port.out.PaymentProviderPort;
import com.acme.audit.payment.domain.PaymentResult;
import com.acme.audit.payment.usecases.PaymentApplicationServiceUseCase;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class PaymentApplicationServiceIntegrationTest {
    @Autowired PaymentApplicationServiceUseCase service;
    @Autowired JdbcTemplate jdbc;
    @MockitoBean PaymentProviderPort provider;

    @BeforeEach
    void cleanDatabase() {
        jdbc.update("DELETE FROM payments");
        when(provider.charge(anyString(), anyString(), any(BigDecimal.class), anyString()))
                .thenReturn(new PaymentProviderPort.ProviderCharge("PROV-TX-1", "APPROVED"));
    }

    @Test
    void sameKeyCreatesOnePaymentUnderConcurrency() throws Exception {
        String key = "ORDER-CONCURRENT-100-PAYMENT";
        var request = new CreatePaymentCommand("ORDER-CONCURRENT-100", new BigDecimal("250000.00"), "COP");
        int workers = 12;
        var pool = Executors.newFixedThreadPool(workers);
        var ready = new CountDownLatch(workers);
        var start = new CountDownLatch(1);
        var futures = new ArrayList<Future<PaymentResult>>();
        try {
            for (int i = 0; i < workers; i++) futures.add(pool.submit(() -> {
                ready.countDown();
                start.await();
                return service.process(key, request);
            }));
            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            var results = new ArrayList<PaymentResult>();
            for (var future : futures) results.add(future.get(10, TimeUnit.SECONDS));
            assertThat(results).extracting(PaymentResult::paymentId).containsOnly(results.get(0).paymentId());
            assertThat(service.countPayments()).isEqualTo(1);
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void sameKeyWithDifferentPayloadIsRejected() {
        var original = new CreatePaymentCommand("ORDER-KEY-300", new BigDecimal("100.00"), "COP");
        service.process("ORDER-KEY-300-PAYMENT", original);
        var changed = new CreatePaymentCommand("ORDER-KEY-300", new BigDecimal("200.00"), "COP");
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.process("ORDER-KEY-300-PAYMENT", changed))
                .isInstanceOf(PaymentApplicationServiceUseCase.IdempotencyKeyConflictException.class);
    }

    @Test
    void replayReturnsOriginalPaymentWithoutCallingProviderAgain() {
        var request = new CreatePaymentCommand("ORDER-REPLAY-200", new BigDecimal("100.00"), "COP");
        var first = service.process("ORDER-REPLAY-200-PAYMENT", request);
        var second = service.process("ORDER-REPLAY-200-PAYMENT", request);
        assertThat(second.paymentId()).isEqualTo(first.paymentId());
        assertThat(second.replayed()).isTrue();
        org.mockito.Mockito.verify(provider, org.mockito.Mockito.times(1))
                .charge("ORDER-REPLAY-200-PAYMENT", "ORDER-REPLAY-200", new BigDecimal("100.00"), "COP");
    }
}
