package org.example.akka.java.cluster.singleton;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

class Counter extends AbstractBehavior<CounterProtocol.Command> {

    public static Behavior<CounterProtocol.Command> create() {
        return Behaviors.setup(Counter::new);
    }

    private int value;

    private Counter(ActorContext<CounterProtocol.Command> context) {
        super(context);
        value = 0;
    }

    @Override
    public Receive<CounterProtocol.Command> createReceive() {
        return newReceiveBuilder()
                .onMessageEquals(CounterProtocol.Increment.INSTANCE, this::onIncrement)
                .onMessage(CounterProtocol.GetValue.class, this::onGetValue)
                .onMessageEquals(CounterProtocol.GoodByeCounter.INSTANCE, this::onGoodByCounter)
                .build();
    }

    private Behavior<CounterProtocol.Command> onIncrement() {
        value++;
        return this;
    }

    private Behavior<CounterProtocol.Command> onGetValue(CounterProtocol.GetValue msg) {
        msg.getReplyTo().tell(value);
        return this;
    }

    private Behavior<CounterProtocol.Command> onGoodByCounter() {
        return Behaviors.stopped();
    }
}
