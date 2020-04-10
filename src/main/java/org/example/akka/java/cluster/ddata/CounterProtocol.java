package org.example.akka.java.cluster.ddata;

import akka.actor.typed.ActorRef;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

interface CounterProtocol {

    interface Command {}

    enum Increment implements Command {
        INSTANCE
    }

    @AllArgsConstructor(staticName = "of")
    @Getter
    @EqualsAndHashCode
    @ToString
    final class GetValue implements Command {
        private final ActorRef<Integer> replyTo;
    }

    @AllArgsConstructor(staticName = "of")
    @Getter
    @EqualsAndHashCode
    @ToString
    final class GetCachedValue implements Command {
        private final ActorRef<Integer> replyTo;
    }

    enum Unsubscribe implements Command {
        INSTANCE
    }
}
