package org.example.akka.java.cluster.serialization.custom.stringmanifest;

import akka.actor.typed.ActorRef;
import lombok.Value;

@Value(staticConstructor = "of")
class Ping {
    ActorRef<Pong> replyTo;
}
