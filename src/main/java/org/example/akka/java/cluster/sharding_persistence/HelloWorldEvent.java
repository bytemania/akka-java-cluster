package org.example.akka.java.cluster.sharding_persistence;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.*;

interface HelloWorldEvent {

    @Value
    class Greeted implements CborSerializable {

        static Greeted of(String whom) {
            return new Greeted(whom);
        }

        String whom;

        @JsonCreator
        private Greeted(String whom) {
            this.whom = whom;
        }
    }



}
