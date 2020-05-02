package org.example.akka.java.cluster.delivery.p2p;

import lombok.Value;

import java.math.BigInteger;

class FibonacciConsumerProtocol {

    interface Command {}

    @Value(staticConstructor = "of")
    static class FibonacciNumber implements Command {
        long n;
        BigInteger value;
    }

}
