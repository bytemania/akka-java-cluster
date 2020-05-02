package org.example.akka.java.cluster.delivery.p2p;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.delivery.ProducerController;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import lombok.Value;

import java.math.BigInteger;

class FibonacciProducer extends AbstractBehavior<FibonacciProducerProtocol.Command> {

    private interface InternalCommand extends FibonacciProducerProtocol.Command {}

    @Value(staticConstructor = "of")
    private static class WrappedRequestNext implements InternalCommand {
        ProducerController.RequestNext<FibonacciConsumerProtocol.Command> next;
    }

    public static Behavior<FibonacciProducerProtocol.Command> create(
            ActorRef<ProducerController.Command<FibonacciConsumerProtocol.Command>> producerController) {
        return Behaviors.setup(context -> new FibonacciProducer(context, producerController));
    }

    private long n;
    private BigInteger b;
    private BigInteger a;

    private FibonacciProducer(ActorContext<FibonacciProducerProtocol.Command> context,
                              ActorRef<ProducerController.Command<FibonacciConsumerProtocol.Command>> producerController) {
        super(context);
        ActorRef<ProducerController.RequestNext<FibonacciConsumerProtocol.Command>> requestNextAdapter =
                context.messageAdapter(ProducerController.requestNextClass(), WrappedRequestNext::new);
        producerController.tell(new ProducerController.Start<>(requestNextAdapter));

        n = 0;
        b = BigInteger.ONE;
        a = BigInteger.ZERO;
    }

    @Override
    public Receive<FibonacciProducerProtocol.Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(WrappedRequestNext.class, this::onWrappedRequestNext)
                .build();
    }

    private Behavior<FibonacciProducerProtocol.Command> onWrappedRequestNext(WrappedRequestNext w) {
        getContext().getLog().info("Generated fibonacci {}: {}", n, a);
        w.next.sendNextTo().tell(FibonacciConsumerProtocol.FibonacciNumber.of(n, a));
        if (n == 1000) {
            return Behaviors.stopped();
        } else {
            n++;
            b = a.add(b);
            a = b;
            return this;
        }
    }
}
