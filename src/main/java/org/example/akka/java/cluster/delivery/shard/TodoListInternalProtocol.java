package org.example.akka.java.cluster.delivery.shard;

import akka.actor.typed.ActorRef;
import akka.actor.typed.delivery.ConsumerController;
import lombok.Getter;
import lombok.Value;

interface TodoListInternalProtocol {

    interface InternalCommand extends TodoListProtocol.Command {}

    @Value(staticConstructor = "of")
    class InitialState implements InternalCommand {
        TodoListState todoListState;
    }

    @Value(staticConstructor = "of")
    class SaveSuccess implements InternalCommand {
        ActorRef<ConsumerController.Confirmed> confirmTo;
    }

    class DBError implements InternalCommand {
        static DBError of(Throwable cause) {
            return new DBError(cause);
        }

        @Getter
        private final Exception cause;

        private DBError(Throwable cause) {
            if (cause instanceof Exception) this.cause = (Exception) cause;
            else this.cause = new RuntimeException(cause.getMessage(), cause);
        }
    }

    @Value(staticConstructor = "of")
    class CommandDelivery implements InternalCommand {
        TodoListProtocol.Command command;
        ActorRef<ConsumerController.Confirmed> confirmTo;
    }
}
