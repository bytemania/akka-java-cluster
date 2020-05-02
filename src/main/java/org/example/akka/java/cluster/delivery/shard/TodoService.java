package org.example.akka.java.cluster.delivery.shard;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.cluster.sharding.typed.delivery.ShardingProducerController;

class TodoService {

    static Behavior<TodoServiceProtocol.Command> create(ActorRef<ShardingProducerController.Command<TodoListProtocol.Command>> producerController) {
        return TodoServiceInit.create(producerController);
    }

}
