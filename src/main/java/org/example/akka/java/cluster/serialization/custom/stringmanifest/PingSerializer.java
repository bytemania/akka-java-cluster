package org.example.akka.java.cluster.serialization.custom.stringmanifest;

import akka.actor.ExtendedActorSystem;
import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorRefResolver;
import akka.actor.typed.javadsl.Adapter;
import akka.serialization.SerializerWithStringManifest;

import java.io.NotSerializableException;
import java.nio.charset.StandardCharsets;

class PingSerializer extends SerializerWithStringManifest {

    private final ExtendedActorSystem system;
    private final ActorRefResolver actorRefResolver;

    private final String PING_MANIFEST = "a";
    private final String PONG_MANIFEST = "b";

    PingSerializer(ExtendedActorSystem system) {
        this.system = system;
        actorRefResolver = ActorRefResolver.get(Adapter.toTyped(system));
    }

    @Override
    public int identifier() {
        return 97896;
    }

    @Override
    public String manifest(Object o) {
        if (o instanceof Ping) return PING_MANIFEST;
        else if (o instanceof Pong) return PONG_MANIFEST;
        else throw new IllegalArgumentException("Can't serialize object of type " + o.getClass() + " in [" + getClass().getName() + "]");
    }

    @Override
    public byte[] toBinary(Object o) {
        if (o instanceof Ping) return actorRefResolver.toSerializationFormat(((Ping) o).getReplyTo()).getBytes(StandardCharsets.UTF_8);
        else if (o instanceof Pong) return new byte[0];
        else throw new IllegalArgumentException("Can't serialize object " + o.getClass() + " in [" + getClass().getName() + "]");
    }

    @Override
    public Object fromBinary(byte[] bytes, String manifest) throws NotSerializableException {
        if (PING_MANIFEST.equals(manifest)) {
            String str = new String(bytes, StandardCharsets.UTF_8);
            ActorRef<Pong> ref = actorRefResolver.resolveActorRef(str);
            return Ping.of(ref);
        } else if (PING_MANIFEST.equals(manifest)) {
            return new Pong();
        } else {
            throw new IllegalArgumentException("Unable to handle manifest: " + manifest);
        }
    }
}
