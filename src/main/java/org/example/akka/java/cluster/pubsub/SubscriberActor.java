package org.example.akka.java.cluster.pubsub;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

class SubscriberActor extends AbstractBehavior<Message> {

    public static Behavior<Message> create() {
        return Behaviors.setup(SubscriberActor::new);
    }

    private SubscriberActor(ActorContext<Message> context) {
        super(context);
    }

    @Override
    public Receive<Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(Message.class, this::onMessage)
                .build();
    }

    private Behavior<Message> onMessage(Message message) {
        getContext().getLog().info("Message received {}", message.getMessage());
        return this;
    }
}
