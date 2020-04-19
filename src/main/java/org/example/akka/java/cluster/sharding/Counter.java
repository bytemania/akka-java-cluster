package org.example.akka.java.cluster.sharding;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

class Counter extends AbstractBehavior<CounterProtocol.Command> {

    static Behavior<CounterProtocol.Command> create(String entityId) {
        return Behaviors.setup(context -> new Counter(context, entityId));
    }

    private final String entityId;
    private int value = 0;

    public Counter(ActorContext<CounterProtocol.Command> context, String entityId) {
        super(context);
        this.entityId = entityId;
    }

    @Override
    public Receive<CounterProtocol.Command> createReceive() {
        return newReceiveBuilder()
                .onMessageEquals(CounterProtocol.Increment.INSTANCE, this::onIncrement)
                .onMessage(CounterProtocol.GetValue.class, this::onGetValue)
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
}
