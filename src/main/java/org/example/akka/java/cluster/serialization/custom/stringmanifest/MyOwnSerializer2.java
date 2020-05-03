package org.example.akka.java.cluster.serialization.custom.stringmanifest;

import akka.serialization.SerializerWithStringManifest;

import java.io.NotSerializableException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

class MyOwnSerializer2 extends SerializerWithStringManifest {

    private static final String CUSTOMER_MANIFEST = "customer";
    private static final String USER_MANIFEST = "user";
    private static final String UTF_8 = StandardCharsets.UTF_8.name();

    @Override
    public int identifier() {
        return 1234567; //0-40 akka internal
    }

    @Override
    public String manifest(Object o) {
        if (o instanceof Customer) return CUSTOMER_MANIFEST;
        else if (o instanceof User) return USER_MANIFEST;
        else throw new IllegalArgumentException("Unknown type: " + o);
    }

    @Override
    public byte[] toBinary(Object o) {
        try {
            if (o instanceof Customer) return ((Customer) o).getName().getBytes(UTF_8);
            else if (o instanceof User) return ((User) o).getName().getBytes(UTF_8);
            else throw new IllegalArgumentException("Unknown type: " + o);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public Object fromBinary(byte[] bytes, String manifest) throws NotSerializableException {
        try {
          if (manifest.equals(CUSTOMER_MANIFEST)) return Customer.of(new String(bytes, UTF_8));
          else if (manifest.equals(USER_MANIFEST)) return User.of(new String(bytes, UTF_8));
          else throw new IllegalArgumentException("Unknown manifest: " + manifest);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
