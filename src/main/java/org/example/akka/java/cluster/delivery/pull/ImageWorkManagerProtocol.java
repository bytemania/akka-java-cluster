package org.example.akka.java.cluster.delivery.pull;

import akka.actor.typed.ActorRef;
import lombok.Value;

import java.util.Optional;
import java.util.UUID;

interface ImageWorkManagerProtocol {

    interface Command {}

    @Value(staticConstructor = "of")
    class Convert implements Command {
        String fromFormat;
        String toFormat;
        byte[] image;
    }

    @Value(staticConstructor = "of")
    class GetResult implements Command {
        UUID resultId;
        ActorRef<Optional<byte[]>> reply;
    }

}
