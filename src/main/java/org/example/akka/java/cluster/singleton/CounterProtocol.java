package org.example.akka.java.cluster.singleton;

import akka.actor.typed.ActorRef;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;

interface CounterProtocol {

    interface Command {}

    enum Increment implements Command, Serializable {
        INSTANCE
    }

    @AllArgsConstructor(staticName = "of")
    @Getter
    @EqualsAndHashCode
    @ToString
    class GetValue implements Command, Serializable {
        private final ActorRef<Integer> replyTo;
    }

    enum GoodByeCounter implements Command, Serializable {
        INSTANCE
    }
}
