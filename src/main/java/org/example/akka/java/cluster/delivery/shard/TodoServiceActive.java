package org.example.akka.java.cluster.delivery.shard;

import akka.Done;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.cluster.sharding.typed.delivery.ShardingProducerController;

import java.time.Duration;

class TodoServiceActive extends AbstractBehavior<TodoServiceProtocol.Command> {

    static Behavior<TodoServiceProtocol.Command> create(ShardingProducerController.RequestNext<TodoListProtocol.Command> requestNext) {
        return Behaviors.setup(context -> new TodoServiceActive(context, requestNext));
    }

    private ShardingProducerController.RequestNext<TodoListProtocol.Command> requestNext;

    private TodoServiceActive(
            ActorContext<TodoServiceProtocol.Command> context,
            ShardingProducerController.RequestNext<TodoListProtocol.Command> requestNext) {
        super(context);
        this.requestNext = requestNext;
    }

    @Override
    public Receive<TodoServiceProtocol.Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(TodoServiceInternalProtocol.WrappedRequestNext.class, this::onRequestNext)
                .onMessage(TodoServiceProtocol.UpdateTodo.class, this::onUpdateTodo)
                .onMessage(TodoServiceInternalProtocol.Confirmed.class, this::onConfirmed)
                .onMessage(TodoServiceInternalProtocol.TimedOut.class, this::onTimedOut)
                .build();
    }

    private Behavior<TodoServiceProtocol.Command> onRequestNext(TodoServiceInternalProtocol.WrappedRequestNext w) {
        requestNext = w.getNext();
        return this;
    }

    private Behavior<TodoServiceProtocol.Command> onUpdateTodo(TodoServiceProtocol.UpdateTodo command) {
        Integer buffered = requestNext.getBufferedForEntitiesWithoutDemand().get(command.getListId());
        if (buffered != null && buffered >= 100) {
            command.getReplyTo().tell(TodoServiceProtocol.Response.REJECTED);
        } else {
            TodoListProtocol.Command requestMsg;
            if (command.isCompleted()) requestMsg = TodoListProtocol.CompleteTask.of(command.getItem());
            else requestMsg = TodoListProtocol.AddTask.of(command.getItem());
            getContext()
                    .ask(
                            Done.class,
                            requestNext.askNextTo(),
                            Duration.ofSeconds(5),
                            askReplyTo -> new ShardingProducerController.MessageWithConfirmation<>(command.getListId(), requestMsg, askReplyTo),
                            (done, exc) -> {
                                if (exc == null) return TodoServiceInternalProtocol.Confirmed.of(command.getReplyTo());
                                else return TodoServiceInternalProtocol.TimedOut.of(command.getReplyTo());
                            });
        }
        return this;
    }

    private Behavior<TodoServiceProtocol.Command> onConfirmed(TodoServiceInternalProtocol.Confirmed confirmed) {
        confirmed.getOriginalReplyTo().tell(TodoServiceProtocol.Response.ACCEPT);
        return this;
    }

    private Behavior<TodoServiceProtocol.Command> onTimedOut(TodoServiceInternalProtocol.TimedOut timedOut) {
        timedOut.getOriginalReplyTo().tell(TodoServiceProtocol.Response.MAYBE_ACCEPTED);
        return this;
    }
}
