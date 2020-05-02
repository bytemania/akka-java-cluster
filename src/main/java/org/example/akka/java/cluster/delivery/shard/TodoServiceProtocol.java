package org.example.akka.java.cluster.delivery.shard;

import akka.actor.typed.ActorRef;
import lombok.Value;

interface TodoServiceProtocol {

    interface Command {}

    @Value(staticConstructor = "of")
    class UpdateTodo implements Command {
        String listId;
        String item;
        boolean completed;
        ActorRef<Response> replyTo;
    }

    enum Response {
        ACCEPT,
        REJECTED,
        MAYBE_ACCEPTED
    }

}
