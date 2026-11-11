package com.acme.audit.payment.usecases;



import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import com.acme.audit.payment.application.CreatePaymentCommand;
import com.acme.audit.payment.application.port.out.PaymentProviderPort;
import com.acme.audit.payment.application.port.out.PaymentRepository;
import com.acme.audit.payment.application.port.out.PaymentRepository.StoredPayment;
import com.acme.audit.payment.domain.PaymentResult;

@ExtendWith(MockitoExtension.class)
class PaymentApplicationServiceUseCaseTest {

    private static final String IDEMPOTENCY_KEY = "ORDER-123-PAYMENT";

    private static final String ORDER_ID = "ORDER-123";

    private static final BigDecimal AMOUNT = new BigDecimal("250.00");

    private static final String CURRENCY = "COP";

    private static final String PAYMENT_STATUS = "APPROVED";

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentProviderPort paymentProvider;

    private PaymentApplicationServiceUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new PaymentApplicationServiceUseCase(
                paymentRepository,
                paymentProvider
        );
    }

    @Test
    void shouldProcessAndPersistPaymentWhenIdempotencyKeyDoesNotExist() {
        CreatePaymentCommand command = createCommand();
        PaymentProviderPort.ProviderCharge providerCharge =
                mockProviderCharge(PAYMENT_STATUS);
        PaymentResult expectedResult = mock(PaymentResult.class);

        when(paymentRepository.findByIdempotencyKey(IDEMPOTENCY_KEY))
                .thenReturn(Optional.empty());

        when(paymentProvider.charge(
                IDEMPOTENCY_KEY,
                ORDER_ID,
                AMOUNT,
                CURRENCY
        )).thenReturn(providerCharge);

        when(paymentRepository.save(
                IDEMPOTENCY_KEY,
                ORDER_ID,
                AMOUNT,
                CURRENCY,
                PAYMENT_STATUS
        )).thenReturn(expectedResult);

        PaymentResult result = useCase.process(IDEMPOTENCY_KEY, command);

        assertThat(result).isSameAs(expectedResult);

        InOrder inOrder = inOrder(paymentRepository, paymentProvider);

        inOrder.verify(paymentRepository)
                .findByIdempotencyKey(IDEMPOTENCY_KEY);

        inOrder.verify(paymentProvider)
                .charge(IDEMPOTENCY_KEY, ORDER_ID, AMOUNT, CURRENCY);

        inOrder.verify(paymentRepository)
                .save(
                        IDEMPOTENCY_KEY,
                        ORDER_ID,
                        AMOUNT,
                        CURRENCY,
                        PAYMENT_STATUS
                );

        verifyNoMoreInteractions(paymentRepository, paymentProvider);
    }

    @Test
    void shouldReturnExistingPaymentWithoutCallingProviderWhenPayloadMatches() {
        CreatePaymentCommand command = createCommand();
        PaymentResult expectedResult = mock(PaymentResult.class);

        StoredPayment storedPayment = createStoredPayment(
                ORDER_ID,
                new BigDecimal("250.00"),
                CURRENCY,
                expectedResult
        );

        when(paymentRepository.findByIdempotencyKey(IDEMPOTENCY_KEY))
                .thenReturn(Optional.of(storedPayment));

        PaymentResult result = useCase.process(IDEMPOTENCY_KEY, command);

        assertThat(result).isSameAs(expectedResult);

        verify(paymentRepository).findByIdempotencyKey(IDEMPOTENCY_KEY);
        verify(paymentProvider, never()).charge(
                IDEMPOTENCY_KEY,
                ORDER_ID,
                AMOUNT,
                CURRENCY
        );

        verifyNoMoreInteractions(paymentRepository, paymentProvider);
    }

    @Test
    void shouldAcceptEquivalentAmountsWithDifferentScales() {
        CreatePaymentCommand command = createCommand();
        PaymentResult expectedResult = mock(PaymentResult.class);

        StoredPayment storedPayment = createStoredPayment(
                ORDER_ID,
                new BigDecimal("250.0"),
                CURRENCY,
                expectedResult
        );

        when(paymentRepository.findByIdempotencyKey(IDEMPOTENCY_KEY))
                .thenReturn(Optional.of(storedPayment));

        PaymentResult result = useCase.process(IDEMPOTENCY_KEY, command);

        assertThat(result).isSameAs(expectedResult);

        verify(paymentRepository).findByIdempotencyKey(IDEMPOTENCY_KEY);
        verifyNoMoreInteractions(paymentRepository, paymentProvider);
    }

      

    

    @Test
    void shouldReturnExistingPaymentWhenConcurrentRequestWinsUniqueKeyRace() {
        CreatePaymentCommand command = createCommand();
        PaymentProviderPort.ProviderCharge providerCharge =
                mockProviderCharge(PAYMENT_STATUS);
        PaymentResult expectedResult = mock(PaymentResult.class);

        StoredPayment storedPayment = createStoredPayment(
                ORDER_ID,
                AMOUNT,
                CURRENCY,
                expectedResult
        );

        DuplicateKeyException duplicateKeyException =
                new DuplicateKeyException("Idempotency key already exists");

        when(paymentRepository.findByIdempotencyKey(IDEMPOTENCY_KEY))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(storedPayment));

        when(paymentProvider.charge(
                IDEMPOTENCY_KEY,
                ORDER_ID,
                AMOUNT,
                CURRENCY
        )).thenReturn(providerCharge);

        when(paymentRepository.save(
                IDEMPOTENCY_KEY,
                ORDER_ID,
                AMOUNT,
                CURRENCY,
                PAYMENT_STATUS
        )).thenThrow(duplicateKeyException);

        PaymentResult result = useCase.process(IDEMPOTENCY_KEY, command);

        assertThat(result).isSameAs(expectedResult);

        InOrder inOrder = inOrder(paymentRepository, paymentProvider);

        inOrder.verify(paymentRepository)
                .findByIdempotencyKey(IDEMPOTENCY_KEY);

        inOrder.verify(paymentProvider)
                .charge(IDEMPOTENCY_KEY, ORDER_ID, AMOUNT, CURRENCY);

        inOrder.verify(paymentRepository)
                .save(
                        IDEMPOTENCY_KEY,
                        ORDER_ID,
                        AMOUNT,
                        CURRENCY,
                        PAYMENT_STATUS
                );

        inOrder.verify(paymentRepository)
                .findByIdempotencyKey(IDEMPOTENCY_KEY);

        verifyNoMoreInteractions(paymentRepository, paymentProvider);
    }

   
   

    @Test
    void shouldReturnPaymentCountFromRepository() {
        long expectedCount = 42L;

        when(paymentRepository.count()).thenReturn(expectedCount);

        long result = useCase.countPayments();

        assertThat(result).isEqualTo(expectedCount);

        verify(paymentRepository).count();
        verifyNoMoreInteractions(paymentRepository, paymentProvider);
    }
    
   

    private CreatePaymentCommand createCommand() {
        return new CreatePaymentCommand(
                ORDER_ID,
                AMOUNT,
                CURRENCY
        );
    }

    private PaymentProviderPort.ProviderCharge mockProviderCharge(String status) {
        PaymentProviderPort.ProviderCharge charge =
                mock(PaymentProviderPort.ProviderCharge.class);

        when(charge.status()).thenReturn(status);

        return charge;
    }

    private StoredPayment createStoredPayment(
            String orderId,
            BigDecimal amount,
            String currency,
            PaymentResult result) {

        StoredPayment storedPayment = mock(StoredPayment.class);

        when(storedPayment.orderId()).thenReturn(orderId);
        when(storedPayment.amount()).thenReturn(amount);
        when(storedPayment.currency()).thenReturn(currency);
        when(storedPayment.result()).thenReturn(result);

        return storedPayment;
    }
}
