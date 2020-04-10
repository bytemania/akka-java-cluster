package org.example.akka.java.cluster.ddata;

import akka.actor.typed.ActorRef;
import akka.cluster.ddata.GCounter;
import akka.cluster.ddata.typed.javadsl.Replicator;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

interface CounterInternalProtocol {

    interface InternalCommand extends CounterProtocol.Command {}

    @AllArgsConstructor(staticName = "of")
    @Getter
    @EqualsAndHashCode
    @ToString
    final class InternalUpdateResponse implements InternalCommand {
        private final Replicator.UpdateResponse<GCounter> rsp;
    }

    @AllArgsConstructor(staticName = "of")
    @Getter
    @EqualsAndHashCode
    @ToString
    final class InternalGetResponse implements InternalCommand {
        private final Replicator.GetResponse<GCounter> rsp;
        private final ActorRef<Integer> replyTo;
    }

    @AllArgsConstructor(staticName = "of")
    @Getter
    @EqualsAndHashCode
    @ToString
    final class InternalSubscribeResponse implements InternalCommand {
        private final Replicator.SubscribeResponse<GCounter> rsp;
    }
}
