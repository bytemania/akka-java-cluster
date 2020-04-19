package org.example.akka.java.cluster.sharding;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.cluster.sharding.typed.ShardingEnvelope;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.Entity;
import akka.cluster.sharding.typed.javadsl.EntityRef;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;

class Guardian extends AbstractBehavior<GuardianProtocol.Command> {

    private static final String COUNTER_TYPE_KEY_NAME = "Counter";
    private static final String COUNTER_ENTITY_NAME = "counter-1";

    public static Behavior<GuardianProtocol.Command> create() {
        return Behaviors.setup(Guardian::new);
    }

    private final ActorRef<ShardingEnvelope<CounterProtocol.Command>> shardRegion;
    private final EntityRef<CounterProtocol.Command> counter;
    private final ActorRef<Integer> printer;

    private Guardian(ActorContext<GuardianProtocol.Command> context) {
        super(context);
        EntityTypeKey<CounterProtocol.Command> typeKey = EntityTypeKey.create(CounterProtocol.Command.class, COUNTER_TYPE_KEY_NAME);
        ClusterSharding sharding = ClusterSharding.get(getContext().getSystem());
        shardRegion = sharding.init(Entity.of(typeKey, ctx -> Counter.create(ctx.getEntityId())));
        counter = sharding.entityRefFor(typeKey, COUNTER_ENTITY_NAME);

        printer = context.spawn(Behaviors.receive(Integer.class)
                .onMessage(Integer.class, i -> {
                    getContext().getLog().info("Printer value: {}", i);
                    return Behaviors.same();
                }).build(), "printer");
    }

    @Override
    public Receive<GuardianProtocol.Command> createReceive() {
        return newReceiveBuilder()
                .onMessageEquals(GuardianProtocol.IncrementCounter.INSTANCE, this::onIncrementCounter)
                .onMessageEquals(GuardianProtocol.IncrementShardRegion.INSTANCE, this::onIncrementCounterShardRegion)
                .build();
    }

    private Behavior<GuardianProtocol.Command> onIncrementCounter() {
        counter.tell(CounterProtocol.Increment.INSTANCE);
        counter.tell(CounterProtocol.GetValue.of(printer));
        return this;
    }

    private Behavior<GuardianProtocol.Command> onIncrementCounterShardRegion() {
        shardRegion.tell(new ShardingEnvelope<>(COUNTER_ENTITY_NAME, CounterProtocol.Increment.INSTANCE));
        shardRegion.tell(new ShardingEnvelope<>(COUNTER_ENTITY_NAME, CounterProtocol.GetValue.of(printer)));
        return this;
    }
}
