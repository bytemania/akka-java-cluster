package org.example.akka.java.cluster.passivation;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.Terminated;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.Entity;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;
import lombok.NoArgsConstructor;

@NoArgsConstructor
class Main {

    private static Behavior<Void> create() {
        return Behaviors.setup(context -> {
            EntityTypeKey<CounterProtocol.Command> typeKey = EntityTypeKey.create(CounterProtocol.Command.class, "Counter");
            ClusterSharding sharding = ClusterSharding.get(context.getSystem());
            sharding.init(Entity.of(typeKey, ctx -> Counter.create(ctx.getShard(), ctx.getEntityId()))
                    .withStopMessage(CounterProtocol.GoodByeCounter.INSTANCE));
            return Behaviors.receive(Void.class).onSignal(Terminated.class, sig -> Behaviors.stopped()).build();
        });
    }

    public static void main(String[] args) {
        ActorSystem.create(Main.create(), "ClusterSystem");
    }

}
