package org.example.akka.java.cluster.sharding;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.akka.java.cluster.util.Util;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
@Slf4j
public class Main {

    private static ActorSystem<GuardianProtocol.Command> createSystem(int port) {
        Config config = ConfigFactory.parseString("akka.remote.artery.canonical.port=" + port)
                .withFallback(ConfigFactory.load());
        return ActorSystem.create(Guardian.create(), "ClusterSystem", config);
    }

    private static List<ActorSystem<GuardianProtocol.Command>> startup(int... ports) {
        return Arrays.stream(ports).mapToObj(Main::createSystem).collect(Collectors.toList());
    }

    private static void setCounters(List<ActorSystem<GuardianProtocol.Command>> systems) throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            int index = Util.randInt(0, systems.size() - 1);
            ActorRef<GuardianProtocol.Command> counter = systems.get(index);
            log.info("Sending increment to actor: {} - {} - {}", i, index, counter.path().toString());
            counter.tell(GuardianProtocol.IncrementCounter.INSTANCE);
            counter.tell(GuardianProtocol.IncrementShardRegion.INSTANCE);
            Thread.sleep(1000);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        List<ActorSystem<GuardianProtocol.Command>> systems = startup(2551, 2552);
        setCounters(systems);
    }
}
