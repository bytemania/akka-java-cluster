package org.example.akka.java.cluster.serialization.custom.stringmanifest;

import lombok.Value;

@Value(staticConstructor = "of")
class User {
    String name;
}
