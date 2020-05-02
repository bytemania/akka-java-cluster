package org.example.akka.java.cluster.pubsub;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.pubsub.Topic;

public class Main {

    private static Behavior<Void> create() {
        return Behaviors.setup(context -> {
            ActorRef<Message> subscriberActor = context.spawn(SubscriberActor.create(), "subscriberActor");
            ActorRef<Topic.Command<Message>> topic = context.spawn(Topic.create(Message.class, "my-topic"), "MyTopic");
            topic.tell(Topic.subscribe(subscriberActor));
            topic.tell(Topic.publish(Message.of("Hello Subscribers")));
            topic.tell(Topic.unsubscribe(subscriberActor));
            return Behaviors.empty();
        });
    }

    public static void main(String[] args) {
        ActorSystem.create(Main.create(), "ClusterSystem");
    }
}
