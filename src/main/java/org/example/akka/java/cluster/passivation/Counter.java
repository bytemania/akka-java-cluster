package org.example.akka.java.cluster.passivation;


import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;

import java.time.Duration;

class Counter extends AbstractBehavior<CounterProtocol.Command> {

    static Behavior<CounterProtocol.Command> create(ActorRef<ClusterSharding.ShardCommand> shard, String entityId) {
        return Behaviors.setup(ctx -> new Counter(ctx, shard, entityId));
    }

    private final ActorRef<ClusterSharding.ShardCommand> shard;
    private final String entityId;
    private int value;

    private Counter(
            ActorContext<CounterProtocol.Command> context,
            ActorRef<ClusterSharding.ShardCommand> shard,
            String entityId) {
        super(context);
        this.shard = shard;
        this.entityId = entityId;
        this.value = 0;
        context.setReceiveTimeout(Duration.ofSeconds(3), CounterProtocol.Idle.INSTANCE);
        context.getLog().info("Counter Created");
    }

    @Override
    public Receive<CounterProtocol.Command> createReceive() {
        return newReceiveBuilder()
                .onMessageEquals(CounterProtocol.Increment.INSTANCE, this::onIncrement)
                .onMessage(CounterProtocol.GetValue.class, this::onGetValue)
                .onMessageEquals(CounterProtocol.Idle.INSTANCE, this::onIdle)
                .onMessageEquals(CounterProtocol.GoodByeCounter.INSTANCE, this::onGoodByeCounter)
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

    private Behavior<CounterProtocol.Command> onIdle() {

        shard.tell(new ClusterSharding.Passivate<>(getContext().getSelf()));
        return this;
    }

    private Behavior<CounterProtocol.Command> onGoodByeCounter() {
        return Behaviors.stopped();
    }
}
