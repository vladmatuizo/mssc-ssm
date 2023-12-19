package guru.springframework.msscssm.service;

import guru.springframework.msscssm.domain.Payment;
import guru.springframework.msscssm.domain.PaymentEvent;
import guru.springframework.msscssm.domain.PaymentState;
import guru.springframework.msscssm.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Component;

import static guru.springframework.msscssm.config.StateMachineConfig.PAYMENT_ID_HEADER;

@Component
@RequiredArgsConstructor
public class PaymentStateChangeInterceptor extends StateMachineInterceptorAdapter<PaymentState, PaymentEvent> {


    private final PaymentRepository paymentRepository;

    @Override
    public void preStateChange(State<PaymentState, PaymentEvent> state, Message<PaymentEvent> message,
                               Transition<PaymentState, PaymentEvent> transition,
                               StateMachine<PaymentState, PaymentEvent> stateMachine,
                               StateMachine<PaymentState, PaymentEvent> rootStateMachine) {
        if (message != null) {
            Object paymentId = message.getHeaders().getOrDefault(PAYMENT_ID_HEADER, -1L);
            if (paymentId instanceof Long) {
                Payment payment = paymentRepository.findById((Long) paymentId).orElseThrow();
                payment.setState(state.getId());

                paymentRepository.save(payment);
            }
        }
    }
}
