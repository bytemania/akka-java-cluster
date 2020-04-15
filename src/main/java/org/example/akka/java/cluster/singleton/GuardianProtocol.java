package org.example.akka.java.cluster.singleton;

public interface GuardianProtocol {

    interface Command {}

    enum Run implements Command {
        INSTANCE
    }
}
