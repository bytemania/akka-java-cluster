package org.example.akka.java.cluster.sharding_persistence;

import akka.actor.typed.ActorRef;
import lombok.*;

interface HelloWorldProtocol {

    interface Command extends CborSerializable {}

    @AllArgsConstructor(staticName = "of")
    @Value
    final class Greet implements Command, CborSerializable {
        private final String whom;
        private final ActorRef<Greeting> replyTo;
    }

    @AllArgsConstructor(staticName = "of")
    @Value
    final class Greeting implements Command, CborSerializable {
        private final String whom;
        private final int numberOfPeople;
    }
}
