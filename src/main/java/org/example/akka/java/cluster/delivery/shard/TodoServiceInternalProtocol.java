package org.example.akka.java.cluster.delivery.shard;

import akka.actor.typed.ActorRef;
import akka.cluster.sharding.typed.delivery.ShardingProducerController;
import lombok.Value;

interface TodoServiceInternalProtocol {

    interface InternalCommand extends TodoServiceProtocol.Command {}

    @Value(staticConstructor = "of")
    class Confirmed implements InternalCommand {
        ActorRef<TodoServiceProtocol.Response> originalReplyTo;
    }

    @Value(staticConstructor = "of")
    class TimedOut implements InternalCommand {
        ActorRef<TodoServiceProtocol.Response> originalReplyTo;
    }

    @Value(staticConstructor = "of")
    class WrappedRequestNext implements InternalCommand {
        ShardingProducerController.RequestNext<TodoListProtocol.Command> next;
    }



}
