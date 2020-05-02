package org.example.akka.java.cluster.delivery.shard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class TodoListState {

    static TodoListState of(List<String> tasks) {
        return new TodoListState(tasks);
    }

    private final List<String> tasks;

    private TodoListState(List<String> tasks) {
        this.tasks = Collections.unmodifiableList(tasks);
    }

    TodoListState add(String task) {
        ArrayList<String> copy = new ArrayList<>(tasks);
        copy.add(task);
        return new TodoListState(copy);
    }

    TodoListState remove(String task) {
        ArrayList<String> copy = new ArrayList<>(tasks);
        copy.remove(task);
        return new TodoListState(copy);
    }

}
