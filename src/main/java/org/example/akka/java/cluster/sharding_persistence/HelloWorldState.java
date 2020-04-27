package org.example.akka.java.cluster.sharding_persistence;

import java.util.HashSet;
import java.util.Set;

interface HelloWorldState {

    final class KnownPeople implements CborSerializable {
        static KnownPeople EMPTY = new KnownPeople();

        private Set<String> names;

        private KnownPeople() {
            names = Set.of();
        }

        private KnownPeople(Set<String> names) {
            this.names = names;
        }

        KnownPeople add(String name) {
            Set<String> newNames = new HashSet<>(names);
            newNames.add(name);
            return new KnownPeople(newNames);
        }

        int numberOfPeople() {
            return names.size();
        }
    }
}
