package org.example.akka.java.cluster.delivery.shard;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.delivery.ConsumerController;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

class TodoListInit extends AbstractBehavior<TodoListProtocol.Command> {

    static Behavior<TodoListProtocol.Command> create(String id, DB db,
                                                     ActorRef<ConsumerController.Start<TodoListProtocol.Command>> consumerController) {
        return Behaviors.setup(context -> {
            context.pipeToSelf(db.load(id), (state, exc) -> {
                if (exc == null) return TodoListInternalProtocol.InitialState.of(state);
                else return TodoListInternalProtocol.DBError.of(exc);
            });
            return new TodoListInit(context, id, db, consumerController);
        });
    }

    private final String id;
    private final DB db;
    private final ActorRef<ConsumerController.Start<TodoListProtocol.Command>> consumerController;

    private TodoListInit(ActorContext<TodoListProtocol.Command> context, String id, DB db,
                         ActorRef<ConsumerController.Start<TodoListProtocol.Command>> consumerController) {
        super(context);
        this.id = id;
        this.db = db;
        this.consumerController = consumerController;
    }

    @Override
    public Receive<TodoListProtocol.Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(TodoListInternalProtocol.InitialState.class, this::onInitialState)
                .onMessage(TodoListInternalProtocol.DBError.class, this::onDBError)
                .build();
    }

    private Behavior<TodoListProtocol.Command> onInitialState(TodoListInternalProtocol.InitialState initialState) {
        ActorRef<ConsumerController.Delivery<TodoListProtocol.Command>> deliveryAdapter = getContext().messageAdapter(
                ConsumerController.deliveryClass(),
                d -> TodoListInternalProtocol.CommandDelivery.of(d.message(), d.confirmTo()));
        consumerController.tell(new ConsumerController.Start<>(deliveryAdapter));
        return TodoListActive.create(id, db, initialState.getTodoListState());
    }

    private Behavior<TodoListProtocol.Command> onDBError(TodoListInternalProtocol.DBError error) throws Exception {
        throw error.getCause();
    }
}
