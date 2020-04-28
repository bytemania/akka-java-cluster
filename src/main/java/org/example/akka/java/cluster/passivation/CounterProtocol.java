package org.example.akka.java.cluster.passivation;

import akka.actor.typed.ActorRef;
import lombok.Value;

interface CounterProtocol {
    interface Command {}

    enum Idle implements Command {
        INSTANCE
    }

    enum GoodByeCounter implements Command {
        INSTANCE
    }

    enum Increment implements Command {
        INSTANCE
    }

    @Value(staticConstructor = "of")
    class GetValue implements Command {
        ActorRef<Integer> replyTo;
    }



}
