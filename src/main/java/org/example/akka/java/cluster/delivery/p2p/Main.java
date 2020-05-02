package org.example.akka.java.cluster.delivery.p2p;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.delivery.ConsumerController;
import akka.actor.typed.delivery.ProducerController;
import akka.actor.typed.javadsl.Behaviors;

import java.util.Optional;
import java.util.UUID;

public class Main {

    private static Behavior<Void> create() {
        return Behaviors.setup(context -> {

            ActorRef<ConsumerController.Command<FibonacciConsumerProtocol.Command>> consumerController =
                context.spawn(ConsumerController.create(), "consumerController");
            context.spawn(FibonacciConsumer.create(consumerController), "consumer");

            String producerId = "fibonacci-" + UUID.randomUUID();
            ActorRef<ProducerController.Command<FibonacciConsumerProtocol.Command>> producerController =
                    context.spawn(ProducerController.create(FibonacciConsumerProtocol.Command.class, producerId,
                            Optional.empty()), "producerController");
            context.spawn(FibonacciProducer.create(producerController), "producer");

            consumerController.tell(new ConsumerController.RegisterToProducerController<>(producerController));

            return Behaviors.same();
        });
    }

    public static void main(String[] args) {
        ActorSystem.create(Main.create(), "ClusterSystem");
    }
}
