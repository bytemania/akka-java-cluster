package org.example.akka.java.cluster.delivery.p2p;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.delivery.ConsumerController;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import lombok.Value;

class FibonacciConsumer extends AbstractBehavior<FibonacciConsumerProtocol.Command> {

    private interface InternalCommand extends FibonacciConsumerProtocol.Command {}

    @Value(staticConstructor = "of")
    private static class WrappedDelivery implements InternalCommand {
        ConsumerController.Delivery<FibonacciConsumerProtocol.Command> delivery;
    }

    public static Behavior<FibonacciConsumerProtocol.Command> create(
            ActorRef<ConsumerController.Command<FibonacciConsumerProtocol.Command>> consumerController) {
        return Behaviors.setup(context -> new FibonacciConsumer(context, consumerController));
    }

    public FibonacciConsumer(ActorContext<FibonacciConsumerProtocol.Command> context,
                             ActorRef<ConsumerController.Command<FibonacciConsumerProtocol.Command>> consumerController) {
        super(context);
        ActorRef<ConsumerController.Delivery<FibonacciConsumerProtocol.Command>> deliveryAdapter =
                context.messageAdapter(ConsumerController.deliveryClass(), WrappedDelivery::new);
        consumerController.tell(new ConsumerController.Start<>(deliveryAdapter));
    }

    @Override
    public Receive<FibonacciConsumerProtocol.Command> createReceive() {
        return newReceiveBuilder().onMessage(WrappedDelivery.class, this::onDelivery).build();
    }

    private Behavior<FibonacciConsumerProtocol.Command> onDelivery(WrappedDelivery w) {
        FibonacciConsumerProtocol.FibonacciNumber number = (FibonacciConsumerProtocol.FibonacciNumber) w.delivery.message();
        getContext().getLog().info("Processed fibonacci {}: {}", number.getN(), number.getValue());
        w.delivery.confirmTo().tell(ConsumerController.confirmed());
        return this;
    }
}
