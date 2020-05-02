package org.example.akka.java.cluster.delivery.shard;

import akka.Done;
import akka.actor.Address;
import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.delivery.ConsumerController;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.Cluster;
import akka.cluster.sharding.typed.ShardingEnvelope;
import akka.cluster.sharding.typed.delivery.ShardingConsumerController;
import akka.cluster.sharding.typed.delivery.ShardingProducerController;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.Entity;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class Main {

    private static final DB db = new DB() {
        @Override
        public CompletionStage<Done> save(String id, TodoListState todoListState) {
            return CompletableFuture.completedFuture(Done.done());
        }

        @Override
        public CompletionStage<TodoListState> load(String id) {
            return CompletableFuture.completedFuture(TodoListState.of(List.of("task1", "task2", "task3")));
        }
    };

    private static Behavior<Void> create() {
        return Behaviors.setup(context -> {

            ActorSystem<Void> system = context.getSystem();

            EntityTypeKey<ConsumerController.SequencedMessage<TodoListProtocol.Command>> entityTypeKey =
                    EntityTypeKey.create(ShardingConsumerController.entityTypeKeyClass(), "todo");

            ActorRef<ShardingEnvelope<ConsumerController.SequencedMessage<TodoListProtocol.Command>>> region =
                    ClusterSharding.get(system)
                            .init(Entity.of(
                                    entityTypeKey,
                                    entityContext -> ShardingConsumerController.create(start ->
                                            TodoList.create(entityContext.getEntityId(), db, start))));

            Address selfAddress = Cluster.get(system).selfMember().address();
            String producerId = "todo-producer-" + selfAddress.host() + ":" + selfAddress.getPort();
            ActorRef<ShardingProducerController.Command<TodoListProtocol.Command>> producerController =
                    context.spawn(
                            ShardingProducerController.create(TodoListProtocol.Command.class, producerId, region, Optional.empty()),
                            "producerController");
            context.spawn(TodoService.create(producerController), "producer");

            return Behaviors.same();
        });
    }

    public static void main(String[] args) {
        ActorSystem.create(Main.create(), "ClusterSystem");
    }

}
