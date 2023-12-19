package guru.springframework.msscssm.service;

import guru.springframework.msscssm.domain.Payment;
import guru.springframework.msscssm.domain.PaymentEvent;
import guru.springframework.msscssm.domain.PaymentState;
import guru.springframework.msscssm.repository.PaymentRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
class DefaultPaymentServiceTest {

    @Autowired
    PaymentService paymentService;

    @Autowired
    PaymentRepository paymentRepository;

    Payment payment;

    @BeforeEach
    void setUp() {
        payment = Payment.builder().amount(BigDecimal.TEN).build();
    }

    @Test
    void testPreAuthorize() {
        Payment createdPayment = paymentService.create(payment);

        log.info("State of created payment: {}", createdPayment.getState());
        assertEquals(PaymentState.NEW, createdPayment.getState());

        StateMachine<PaymentState, PaymentEvent> stateMachine = paymentService.preAuthorize(createdPayment.getId());
        log.info("State of state machine after pre auth: {}", stateMachine.getState().getId());

        Payment preAuthedPayment = paymentRepository.findById(createdPayment.getId()).orElseThrow();
        log.info("State of pre auth payment from database: {}", preAuthedPayment.getState());

        assertEquals(stateMachine.getState().getId(), preAuthedPayment.getState());
    }

    @RepeatedTest(10)
    void testAuthorize() {
        Payment createdPayment = paymentService.create(payment);

        StateMachine<PaymentState, PaymentEvent> preAuthStateMachine = paymentService.preAuthorize(createdPayment.getId());
        PaymentState preAuthMachineState = preAuthStateMachine.getState().getId();

        if (PaymentState.PRE_AUTH.equals(preAuthMachineState)) {

        StateMachine<PaymentState, PaymentEvent> authStateMachine = paymentService.authorize(createdPayment.getId());

            PaymentState machineState = authStateMachine.getState().getId();
            assertTrue(PaymentState.AUTH.equals(machineState)
                    || PaymentState.AUTH_ERROR.equals(machineState));
        }
    }
}