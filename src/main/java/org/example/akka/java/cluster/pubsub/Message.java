package org.example.akka.java.cluster.pubsub;

import lombok.Value;

@Value(staticConstructor = "of")
class Message {
    String message;
}
