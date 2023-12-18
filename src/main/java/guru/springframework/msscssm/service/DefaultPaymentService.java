package guru.springframework.msscssm.service;

import guru.springframework.msscssm.domain.Payment;
import guru.springframework.msscssm.domain.PaymentEvent;
import guru.springframework.msscssm.domain.PaymentState;
import guru.springframework.msscssm.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultPaymentService implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final StateMachineFactory<PaymentState, PaymentEvent> stateMachineFactory;

    @Override
    public Payment create(Payment payment) {
        payment.setState(PaymentState.NEW);
        return paymentRepository.save(payment);
    }

    @Override
    public StateMachine<PaymentState, PaymentEvent> preAuthorize(Long paymentId) {
        StateMachine<PaymentState, PaymentEvent> stateMachine = restoreStateMachineFromDb(paymentId);

        return null;
    }

    @Override
    public StateMachine<PaymentState, PaymentEvent> authorize(Long paymentId) {
        StateMachine<PaymentState, PaymentEvent> stateMachine = restoreStateMachineFromDb(paymentId);

        return null;
    }

    @Override
    public StateMachine<PaymentState, PaymentEvent> declineAuthorization(Long paymentId) {
        StateMachine<PaymentState, PaymentEvent> stateMachine = restoreStateMachineFromDb(paymentId);

        return null;
    }

    private StateMachine<PaymentState, PaymentEvent> restoreStateMachineFromDb(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId).orElseThrow();

        StateMachine<PaymentState, PaymentEvent> stateMachine =
                stateMachineFactory.getStateMachine(Long.toString(payment.getId()));

        stateMachine.stop();

        stateMachine.getStateMachineAccessor()
                        .doWithAllRegions(access -> access.resetStateMachine(
                                new DefaultStateMachineContext<>(payment.getState(), null, null, null)));

        stateMachine.start();

        return stateMachine;
    }
}
