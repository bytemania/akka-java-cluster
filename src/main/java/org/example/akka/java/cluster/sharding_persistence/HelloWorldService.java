package org.example.akka.java.cluster.sharding_persistence;


import akka.actor.typed.ActorSystem;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.Entity;
import akka.cluster.sharding.typed.javadsl.EntityRef;
import akka.persistence.typed.PersistenceId;

import java.time.Duration;
import java.util.concurrent.CompletionStage;

public class HelloWorldService {

    private final ActorSystem<?> system;
    private final ClusterSharding sharding;
    private final Duration askTimeout = Duration.ofSeconds(5);

    public HelloWorldService(ActorSystem<?> system) {
        this.system = system;
        this.sharding = ClusterSharding.get(system);

        sharding.init(Entity.of(HelloWorld.ENTITY_TYPE_KEY,
                entityContext -> HelloWorld.create(entityContext.getEntityId(),
                PersistenceId.of(entityContext.getEntityTypeKey().name(), entityContext.getEntityId()))));

    }

    public CompletionStage<Integer> sayHello(String worldId, String whom) {
        EntityRef<HelloWorldProtocol.Command> entityRef = sharding.entityRefFor(HelloWorld.ENTITY_TYPE_KEY, worldId);
        CompletionStage<HelloWorldProtocol.Greeting> result =
                entityRef.ask(replyTo -> HelloWorldProtocol.Greet.of(whom, replyTo), askTimeout);
        return result.thenApply(greeting -> greeting.getNumberOfPeople());
    }

}
