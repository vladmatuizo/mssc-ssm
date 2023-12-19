package guru.springframework.msscssm.config;

import guru.springframework.msscssm.domain.PaymentEvent;
import guru.springframework.msscssm.domain.PaymentState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import java.util.Random;

import static guru.springframework.msscssm.config.StateMachineConfig.PAYMENT_ID_HEADER;

@Slf4j
@Component
public class PaymentActionProvider {

    public Action<PaymentState, PaymentEvent> getPreAuthAction() {
        return stateContext -> {
            log.debug("preAuth action was called!");
            //logic to be made in preAuth
            PaymentEvent paymentEvent;
            if (new Random().nextInt(10) < 8) {
                paymentEvent = PaymentEvent.PRE_AUTH_APPROVED;
            } else {
                log.debug("Payment is declined!");
                paymentEvent = PaymentEvent.PRE_AUTH_DECLINED;
            }
            sendEvent(stateContext, paymentEvent);
        };
    }

    public Action<PaymentState, PaymentEvent> getAuthorizeAction() {
        return stateContext -> {
            log.debug("authorize action was called!");
            //logic to be made in auth
            PaymentEvent paymentEvent;
            if (new Random().nextInt(10) < 8) {
                paymentEvent = PaymentEvent.AUTH_APPROVED;
            } else {
                log.debug("Payment is declined!");
                paymentEvent = PaymentEvent.AUTH_DECLINED;
            }
            sendEvent(stateContext, paymentEvent);
        };
    }

    private void sendEvent(StateContext<PaymentState, PaymentEvent> stateContext, PaymentEvent paymentEvent) {
        stateContext.getStateMachine().sendEvent(
                MessageBuilder.withPayload(paymentEvent)
                        .setHeader(PAYMENT_ID_HEADER, stateContext.getMessageHeader(PAYMENT_ID_HEADER))
                        .build());
    }

    public Action<PaymentState, PaymentEvent> getPreAuthApprovedAction() {
        return stateContext -> log.info("PreAuth for payment {} is approved", stateContext.getMessageHeader(PAYMENT_ID_HEADER));
    }

    public Action<PaymentState, PaymentEvent> getPreAuthDeclinedAction() {
        return stateContext -> log.info("PreAuth for payment {} is declined", stateContext.getMessageHeader(PAYMENT_ID_HEADER));
    }

    public Action<PaymentState, PaymentEvent> getAuthApprovedAction() {
        return stateContext -> log.info("Payment {} is authorized", stateContext.getMessageHeader(PAYMENT_ID_HEADER));
    }

    public Action<PaymentState, PaymentEvent> getAuthDeclinedAction() {
        return stateContext -> log.info("Payment {} is declined", stateContext.getMessageHeader(PAYMENT_ID_HEADER));
    }
}
