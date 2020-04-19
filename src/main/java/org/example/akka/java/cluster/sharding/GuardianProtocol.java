package org.example.akka.java.cluster.sharding;

interface GuardianProtocol {

    interface Command {
    }

    enum IncrementCounter implements Command {
        INSTANCE
    }

    enum IncrementShardRegion implements Command {
        INSTANCE
    }
}
