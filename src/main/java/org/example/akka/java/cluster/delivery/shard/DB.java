package org.example.akka.java.cluster.delivery.shard;

import akka.Done;

import java.util.concurrent.CompletionStage;

interface DB {
    CompletionStage<Done> save(String id, TodoListState todoListState);

    CompletionStage<TodoListState> load(String id);
}
