package guru.springframework.msscssm.config;

import guru.springframework.msscssm.domain.PaymentEvent;
import guru.springframework.msscssm.domain.PaymentState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

import java.util.EnumSet;
import java.util.Random;

@Slf4j
@EnableStateMachineFactory
@RequiredArgsConstructor
@Configuration
public class StateMachineConfig extends StateMachineConfigurerAdapter<PaymentState, PaymentEvent> {

    public static final String PAYMENT_ID_HEADER = "payment_id";

    private final PaymentActionProvider paymentActionProvider;
    private final PaymentGuardProvider paymentGuardProvider;

    @Override
    public void configure(StateMachineStateConfigurer<PaymentState, PaymentEvent> states) throws Exception {
        states.withStates()
                .initial(PaymentState.NEW)
                .states(EnumSet.allOf(PaymentState.class))
                .end(PaymentState.AUTH)
                .end(PaymentState.PRE_AUTH_ERROR)
                .end(PaymentState.AUTH_ERROR);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<PaymentState, PaymentEvent> transitions) throws Exception {
        transitions
                .withExternal().source(PaymentState.NEW).target(PaymentState.NEW)
                    .event(PaymentEvent.PRE_AUTHORIZE)
                    .action(paymentActionProvider.getPreAuthAction())
                    .guard(paymentGuardProvider.getPaymentIdGuard())
                .and()
                .withExternal().source(PaymentState.NEW).target(PaymentState.PRE_AUTH)
                    .event(PaymentEvent.PRE_AUTH_APPROVED)
                    .action(paymentActionProvider.getPreAuthApprovedAction())
                .and()
                .withExternal().source(PaymentState.NEW).target(PaymentState.PRE_AUTH_ERROR)
                    .event(PaymentEvent.PRE_AUTH_DECLINED)
                    .action(paymentActionProvider.getPreAuthDeclinedAction())
                .and()
                // pre-auth to auth state transitions
                .withExternal().source(PaymentState.PRE_AUTH).target(PaymentState.PRE_AUTH)
                    .event(PaymentEvent.AUTHORIZE)
                    .action(paymentActionProvider.getAuthorizeAction())
                .and()
                .withExternal().source(PaymentState.PRE_AUTH).target(PaymentState.AUTH)
                    .event(PaymentEvent.AUTH_APPROVED)
                    .action(paymentActionProvider.getAuthApprovedAction())
                .and()
                .withExternal().source(PaymentState.PRE_AUTH).target(PaymentState.AUTH_ERROR)
                    .event(PaymentEvent.AUTH_DECLINED)
                    .action(paymentActionProvider.getAuthDeclinedAction());
    }

    @Override
    public void configure(StateMachineConfigurationConfigurer<PaymentState, PaymentEvent> config) throws Exception {
        StateMachineListenerAdapter<PaymentState, PaymentEvent> listener = new StateMachineListenerAdapter<>() {
            @Override
            public void stateChanged(State<PaymentState, PaymentEvent> from, State<PaymentState, PaymentEvent> to) {
                log.info("stateChanged(from: {}, to: {})", from, to);
            }
        };

        config.withConfiguration().listener(listener);
    }
}
