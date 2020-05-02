package org.example.akka.java.cluster.delivery.shard;

import lombok.Value;

interface TodoListProtocol {

    interface Command {}

    @Value(staticConstructor = "of")
    class AddTask implements Command {
        String item;
    }

    @Value(staticConstructor = "of")
    class CompleteTask implements Command {
        String item;
    }
}
