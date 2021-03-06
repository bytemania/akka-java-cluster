package org.example.akka.java.cluster.ddata;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.ddata.GCounterKey;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import lombok.extern.slf4j.Slf4j;
import org.example.akka.java.cluster.util.Util;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class Main {

    private static ActorSystem<Integer> createPrinter() {
        Behavior<Integer> behavior = Behaviors.setup(context -> Behaviors.receive(Integer.class)
                .onMessage(Integer.class, i -> {
                    log.info("Received {}", i);
                    return Behaviors.same();
                }).build());

        Config config = ConfigFactory.parseString("akka.remote.artery.canonical.port=2000").withFallback(ConfigFactory.load());
        return ActorSystem.create(behavior, "Printer", config);
    }

    private static ActorSystem<CounterProtocol.Command> createSystem(int port) {
        Config config = ConfigFactory.parseString("akka.remote.artery.canonical.port=" + port).withFallback(ConfigFactory.load());
        return ActorSystem.create(Counter.create(GCounterKey.create("GCounterKey")), "ClusterSystem", config);
    }

    private static List<ActorSystem<CounterProtocol.Command>> startup(int ... ports) {
        return Arrays.stream(ports).mapToObj(Main::createSystem).collect(Collectors.toList());
    }

    private static void counterInfo(List<ActorSystem<CounterProtocol.Command>> systems, ActorSystem<Integer> printer) throws InterruptedException {

        for (ActorSystem<CounterProtocol.Command> system: systems) {
            log.info("System counters: {}", system.address().getPort().get());
            system.tell(CounterProtocol.GetValue.of(printer));
            system.tell(CounterProtocol.GetCachedValue.of(printer));
            Thread.sleep(1000);
        }
    }

    private static void setCounters(List<ActorSystem<CounterProtocol.Command>> systems, ActorSystem<Integer> printer) throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            int index = Util.randInt(0, systems.size() - 1);
            log.info("Sending increment to system: {} - {} - {}", i, index, systems.get(index).address().getPort().get());
            systems.get(index).tell(CounterProtocol.Increment.INSTANCE);
            counterInfo(systems, printer);
            Thread.sleep(2000);
        }
    }

    public static void main(String[] args) throws Exception {
        Config config = ConfigFactory.parseString("akka.remote.artery.canonical.port=2000").withFallback(ConfigFactory.load());
        ActorSystem<Integer> printer = createPrinter();
        List<ActorSystem<CounterProtocol.Command>> systems = startup(2551, 2552);
        setCounters(systems, printer);
    }

}
