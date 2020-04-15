package org.example.akka.java.cluster.singleton;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.cluster.typed.ClusterSingleton;
import akka.cluster.typed.SingletonActor;

import java.time.Duration;

public class Guardian extends AbstractBehavior<GuardianProtocol.Command> {

    public static Behavior<GuardianProtocol.Command> create() {
        return Behaviors.setup(Guardian::new);
    }

    private ActorRef<CounterProtocol.Command> counter;
    private ActorRef<Integer> printer;

    private Guardian(ActorContext<GuardianProtocol.Command> context) {
        super(context);
        ClusterSingleton singleton = ClusterSingleton.get(getContext().getSystem());
        counter = singleton.init(
                SingletonActor.of(
                        Behaviors.supervise(Counter.create())
                                .onFailure(SupervisorStrategy.restartWithBackoff(Duration.ofSeconds(1),
                                        Duration.ofSeconds(10), 0.2)),
                        "GlobalCounter")
                        .withStopMessage(CounterProtocol.GoodByeCounter.INSTANCE));

        printer = context.spawn(Behaviors.receive(Integer.class)
                .onMessage(Integer.class, i -> {
                    getContext().getLog().info("Received {}", i);
                    return Behaviors.same();
                }).build(), "Printer");
    }

    @Override
    public Receive<GuardianProtocol.Command> createReceive() {
        return newReceiveBuilder()
                .onMessageEquals(GuardianProtocol.Run.INSTANCE, this::onRun)
                .build();
    }

    private Behavior<GuardianProtocol.Command> onRun() {
        counter.tell(CounterProtocol.GetValue.of(printer));
        counter.tell(CounterProtocol.Increment.INSTANCE);
        return this;
    }
}
