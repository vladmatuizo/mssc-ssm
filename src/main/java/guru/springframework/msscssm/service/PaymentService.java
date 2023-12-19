package guru.springframework.msscssm.service;

import guru.springframework.msscssm.domain.Payment;
import guru.springframework.msscssm.domain.PaymentEvent;
import guru.springframework.msscssm.domain.PaymentState;
import org.springframework.statemachine.StateMachine;

public interface PaymentService {

    Payment create(Payment payment);
    StateMachine<PaymentState, PaymentEvent> preAuthorize(Long paymentId);
    StateMachine<PaymentState, PaymentEvent> authorize(Long paymentId);
}
