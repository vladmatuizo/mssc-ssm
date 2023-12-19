package guru.springframework.msscssm.service;

import guru.springframework.msscssm.domain.Payment;
import guru.springframework.msscssm.domain.PaymentEvent;
import guru.springframework.msscssm.domain.PaymentState;
import guru.springframework.msscssm.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;

import static guru.springframework.msscssm.config.StateMachineConfig.PAYMENT_ID_HEADER;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultPaymentService implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final StateMachineFactory<PaymentState, PaymentEvent> stateMachineFactory;
    private final PaymentStateChangeInterceptor paymentStateChangeInterceptor;

    @Override
    public Payment create(Payment payment) {
        payment.setState(PaymentState.NEW);
        return paymentRepository.save(payment);
    }

    @Override
    public StateMachine<PaymentState, PaymentEvent> preAuthorize(Long paymentId) {
        StateMachine<PaymentState, PaymentEvent> stateMachine = restoreStateMachineFromDb(paymentId);

        sendEvent(paymentId, stateMachine, PaymentEvent.PRE_AUTHORIZE);
        return stateMachine;
    }

    @Override
    public StateMachine<PaymentState, PaymentEvent> authorize(Long paymentId) {
        StateMachine<PaymentState, PaymentEvent> stateMachine = restoreStateMachineFromDb(paymentId);

        sendEvent(paymentId, stateMachine, PaymentEvent.AUTHORIZE);
        return stateMachine;
    }

    private void sendEvent(Long paymentId, StateMachine<PaymentState, PaymentEvent> stateMachine, PaymentEvent event) {
        Message<PaymentEvent> message = MessageBuilder
                .withPayload(event)
                .setHeader(PAYMENT_ID_HEADER, paymentId)
                .build();

        stateMachine.sendEvent(message);
    }

    private StateMachine<PaymentState, PaymentEvent> restoreStateMachineFromDb(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId).orElseThrow();

        StateMachine<PaymentState, PaymentEvent> stateMachine =
                stateMachineFactory.getStateMachine(Long.toString(payment.getId()));

        stateMachine.stop();

        stateMachine.getStateMachineAccessor()
                .doWithAllRegions(access -> {
                    access.addStateMachineInterceptor(paymentStateChangeInterceptor);
                    access.resetStateMachine(
                            new DefaultStateMachineContext<>(payment.getState(), null, null, null));
                });

        stateMachine.start();

        return stateMachine;
    }
}
