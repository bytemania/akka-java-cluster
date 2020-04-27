package org.example.akka.java.cluster.sharding_persistence;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;
import akka.persistence.typed.PersistenceId;
import akka.persistence.typed.javadsl.CommandHandler;
import akka.persistence.typed.javadsl.Effect;
import akka.persistence.typed.javadsl.EventHandler;
import akka.persistence.typed.javadsl.EventSourcedBehavior;

class HelloWorld extends EventSourcedBehavior<HelloWorldProtocol.Command, HelloWorldEvent.Greeted, HelloWorldState.KnownPeople> {

    static final EntityTypeKey<HelloWorldProtocol.Command> ENTITY_TYPE_KEY =
            EntityTypeKey.create(HelloWorldProtocol.Command.class, "HelloWorld");

    static Behavior<HelloWorldProtocol.Command> create(String entityId, PersistenceId persistenceId) {
        return Behaviors.setup(context -> new HelloWorld(context, entityId, persistenceId));
    }

    public HelloWorld(ActorContext<HelloWorldProtocol.Command> context, String entityId, PersistenceId persistenceId) {
        super(persistenceId);
        context.getLog().info("Starting HelloWorld {}", entityId);
    }

    @Override
    public HelloWorldState.KnownPeople emptyState() {
        return HelloWorldState.KnownPeople.EMPTY;
    }

    @Override
    public CommandHandler<HelloWorldProtocol.Command, HelloWorldEvent.Greeted, HelloWorldState.KnownPeople> commandHandler() {
        return newCommandHandlerBuilder().forAnyState().onCommand(HelloWorldProtocol.Greet.class, this::greet).build();
    }

    @Override
    public EventHandler<HelloWorldState.KnownPeople, HelloWorldEvent.Greeted> eventHandler() {
        return (state, evt) -> state.add(evt.getWhom());
    }

    private Effect<HelloWorldEvent.Greeted, HelloWorldState.KnownPeople> greet(HelloWorldState.KnownPeople state, HelloWorldProtocol.Greet cmd) {
        return Effect()
                .persist(HelloWorldEvent.Greeted.of(cmd.getWhom()))
                .thenRun(newState -> cmd.getReplyTo().tell(HelloWorldProtocol.Greeting.of(cmd.getWhom(),
                        newState.numberOfPeople())));
    }
}
