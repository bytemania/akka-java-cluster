package org.example.akka.java.cluster.delivery.shard;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.delivery.ConsumerController;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

class TodoListActive extends AbstractBehavior<TodoListProtocol.Command> {

    static Behavior<TodoListProtocol.Command> create(String id, DB db, TodoListState todoListState) {
        return Behaviors.setup(context -> new TodoListActive(context, id, db, todoListState));
    }

    private final String id;
    private final DB db;
    private TodoListState todoListState;

    private TodoListActive(ActorContext<TodoListProtocol.Command> context, String id, DB db, TodoListState todoListState) {
        super(context);
        this.id = id;
        this.db = db;
        this.todoListState = todoListState;
    }

    @Override
    public Receive<TodoListProtocol.Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(TodoListInternalProtocol.CommandDelivery.class, this::onDelivery)
                .onMessage(TodoListInternalProtocol.SaveSuccess.class, this::onSaveSuccess)
                .onMessage(TodoListInternalProtocol.DBError.class, this::onDBError)
                .build();
    }

    private Behavior<TodoListProtocol.Command> onDelivery(TodoListInternalProtocol.CommandDelivery delivery) {
        if (delivery.getCommand() instanceof TodoListProtocol.AddTask) {
            TodoListProtocol.AddTask addTask = (TodoListProtocol.AddTask) delivery.getCommand();
            todoListState = todoListState.add(addTask.getItem());
            save(todoListState, delivery.getConfirmTo());
            return this;
        } else if (delivery.getCommand() instanceof TodoListProtocol.CompleteTask) {
            TodoListProtocol.CompleteTask completeTask = (TodoListProtocol.CompleteTask) delivery.getCommand();
            todoListState = todoListState.remove(completeTask.getItem());
            save(todoListState, delivery.getConfirmTo());
            return this;
        } else {
            return Behaviors.unhandled();
        }
    }

    private void save(TodoListState newTodoListState, ActorRef<ConsumerController.Confirmed> confirmTo) {
        getContext().pipeToSelf(db.save(id, newTodoListState), (notUsed, exc) -> {
            if (exc == null) return TodoListInternalProtocol.SaveSuccess.of(confirmTo);
            else return TodoListInternalProtocol.DBError.of(exc);});
    }

    private Behavior<TodoListProtocol.Command> onSaveSuccess(TodoListInternalProtocol.SaveSuccess success) {
        success.getConfirmTo().tell(ConsumerController.confirmed());
        return this;
    }

    private Behavior<TodoListProtocol.Command> onDBError(TodoListInternalProtocol.DBError error) throws Exception {
        throw error.getCause();
    }
}
