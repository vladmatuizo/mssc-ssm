package guru.springframework.msscssm.config;


import guru.springframework.msscssm.domain.PaymentEvent;
import guru.springframework.msscssm.domain.PaymentState;
import org.springframework.statemachine.guard.Guard;
import org.springframework.stereotype.Component;

import static guru.springframework.msscssm.config.StateMachineConfig.PAYMENT_ID_HEADER;

@Component
public class PaymentGuardProvider {

    public Guard<PaymentState, PaymentEvent> getPaymentIdGuard() {
        return stateContext -> stateContext.getMessageHeader(PAYMENT_ID_HEADER) != null;
    }
}
