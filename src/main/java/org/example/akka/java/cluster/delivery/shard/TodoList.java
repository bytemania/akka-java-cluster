package org.example.akka.java.cluster.delivery.shard;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.delivery.ConsumerController;

class TodoList {
    public static Behavior<TodoListProtocol.Command> create(String id, DB db,
                                                            ActorRef<ConsumerController.Start<TodoListProtocol.Command>> consumerController) {
        return TodoListInit.create(id, db, consumerController);
    }


}
