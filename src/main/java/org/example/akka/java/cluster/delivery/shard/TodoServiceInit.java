package org.example.akka.java.cluster.delivery.shard;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.cluster.sharding.typed.delivery.ShardingProducerController;

class TodoServiceInit extends AbstractBehavior<TodoServiceProtocol.Command> {

    static Behavior<TodoServiceProtocol.Command> create(
            ActorRef<ShardingProducerController.Command<TodoListProtocol.Command>> producerController) {
        return Behaviors.setup(context -> {
            ActorRef<ShardingProducerController.RequestNext<TodoListProtocol.Command>> requestNextAdapter =
                    context.messageAdapter(ShardingProducerController.requestNextClass(), TodoServiceInternalProtocol.WrappedRequestNext::of);
            producerController.tell(new ShardingProducerController.Start<>(requestNextAdapter));

            return new TodoServiceInit(context);
        });
    }

    private TodoServiceInit(ActorContext<TodoServiceProtocol.Command> context) {
        super(context);
    }

    @Override
    public Receive<TodoServiceProtocol.Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(TodoServiceInternalProtocol.WrappedRequestNext.class, w -> TodoServiceActive.create(w.getNext()))
                .onMessage(TodoServiceProtocol.UpdateTodo.class, this::onUpdateTodo)
                .build();
    }

    private Behavior<TodoServiceProtocol.Command> onUpdateTodo(TodoServiceProtocol.UpdateTodo command) {
        command.getReplyTo().tell(TodoServiceProtocol.Response.REJECTED);
        return this;
    }
}
