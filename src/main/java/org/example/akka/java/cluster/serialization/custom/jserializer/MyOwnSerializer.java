package org.example.akka.java.cluster.serialization.custom.jserializer;

import akka.serialization.JSerializer;

class MyOwnSerializer extends JSerializer {

    @Override
    public Object fromBinaryJava(byte[] bytes, Class<?> manifest) {
        return null;
    }

    @Override
    public int identifier() {
        return 1234567;
    }

    @Override
    public byte[] toBinary(Object o) {
        return new byte[0];
    }

    @Override
    public boolean includeManifest() {
        return false;
    }
}
