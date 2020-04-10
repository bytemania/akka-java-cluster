package org.example.crdt.test;

import com.netopyr.wurmloch.crdt.*;
import com.netopyr.wurmloch.store.CrdtStore;
import com.netopyr.wurmloch.store.LocalCrdtStore;
import javaslang.collection.Array;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class CrdtTest {

    @Test
    public void growOnlySet() {
        LocalCrdtStore crdtStore1 = new LocalCrdtStore();
        LocalCrdtStore crdtStore2 = new LocalCrdtStore();
        crdtStore1.connect(crdtStore2);

        GSet<String> replica1 = crdtStore1.createGSet("ID_1");
        GSet<String> replica2 = crdtStore2.<String>findGSet("ID_1").get();

        replica1.add("apple");
        replica2.add("banana");

        assertTrue(replica1.containsAll(List.of("apple", "banana")));
        assertTrue(replica2.containsAll(List.of("apple", "banana")));

        crdtStore1.disconnect(crdtStore2);

        replica1.add("strawberry");
        replica2.add("pear");

        assertTrue(replica1.containsAll(List.of("apple", "banana", "strawberry")));
        assertTrue(replica2.containsAll(List.of("apple", "banana", "pear")));

        crdtStore1.connect(crdtStore2);

        assertTrue(replica1.containsAll(List.of("apple", "banana", "strawberry", "pear")));
        assertTrue(replica2.containsAll(List.of("apple", "banana", "strawberry", "pear")));
    }

    @Test
    public void incrementOnlyCounter() {
        LocalCrdtStore crdtStore1 = new LocalCrdtStore();
        LocalCrdtStore crdtStore2 = new LocalCrdtStore();
        crdtStore1.connect(crdtStore2);

        GCounter replica1 = crdtStore1.createGCounter("ID_1");
        GCounter replica2 = crdtStore2.findGCounter("ID_1").get();

        replica1.increment();
        replica2.increment(2L);

        assertEquals(3L, replica1.get());
        assertEquals(3L, replica1.get());

        crdtStore1.disconnect(crdtStore2);

        replica1.increment(3L);
        replica2.increment(5L);

        assertEquals(6L, replica1.get());
        assertEquals(8L, replica1.get());

        crdtStore1.connect(crdtStore2);

        assertEquals(11L, replica1.get());
        assertEquals(11L, replica1.get());
    }

    @Test
    public void pnCounter() {
        LocalCrdtStore crdtStore1 = new LocalCrdtStore();
        LocalCrdtStore crdtStore2 = new LocalCrdtStore();
        crdtStore1.connect(crdtStore2);

        PNCounter replica1 = crdtStore1.createPNCounter("ID_1");
        PNCounter replica2 = crdtStore2.findPNCounter("ID_1").get();

        replica1.increment();
        replica2.decrement(2L);

        assertEquals(-1L, replica1.get());
        assertEquals(-1L, replica2.get());

        crdtStore1.disconnect(crdtStore2);

        replica1.decrement(3L);
        replica2.increment(5L);

        assertEquals(-4L, replica1.get());
        assertEquals(4L, replica2.get());

        crdtStore1.connect(crdtStore2);

        assertEquals(1L, replica1.get());
        assertEquals(1L, replica2.get());
    }

    @Test
    public void lastWriteWins() {
        LocalCrdtStore crdtStore1 = new LocalCrdtStore();
        LocalCrdtStore crdtStore2 = new LocalCrdtStore();
        crdtStore1.connect(crdtStore2);

        LWWRegister<String> replica1 = crdtStore1.createLWWRegister("ID_1");
        LWWRegister<String> replica2 = crdtStore2.<String>findLWWRegister("ID_1").get();

        replica1.set("apple");
        replica2.set("banana");

        assertEquals("banana", replica1.get());
        assertEquals("banana", replica2.get());

        crdtStore1.disconnect(crdtStore2);

        replica1.set("strawberry");
        replica2.set("pear");

        assertEquals("strawberry", replica1.get());
        assertEquals("pear", replica2.get());

        crdtStore1.connect(crdtStore2);
        assertEquals("pear", replica1.get());
        assertEquals("pear", replica2.get());
    }

    @Test
    public void mvregister() {
        LocalCrdtStore crdtStore1 = new LocalCrdtStore();
        LocalCrdtStore crdtStore2 = new LocalCrdtStore();

        crdtStore1.connect(crdtStore2);

        MVRegister<String> replica1 = crdtStore1.createMVRegister("ID_1");
        MVRegister<String> replica2 = crdtStore2.<String>findMVRegister("ID_1").get();

        replica1.set("apple");
        replica2.set("banana");

        assertEquals(Array.of("banana"), replica1.get());
        assertEquals(Array.of("banana"), replica2.get());

        crdtStore1.disconnect(crdtStore2);

        replica1.set("strawberry");
        replica2.set("pear");

        assertEquals(Array.of("strawberry"), replica1.get());
        assertEquals(Array.of("pear"), replica2.get());

        crdtStore1.connect(crdtStore2);

        assertEquals(Array.of("strawberry", "pear"), replica1.get());
        assertEquals(Array.of("pear", "strawberry"), replica2.get());

        replica2.set("orange");

        assertEquals(Array.of("orange"), replica1.get());
        assertEquals(Array.of("orange"), replica2.get());
    }

    @Test
    public void orSet() {
        LocalCrdtStore crdtStore1 = new LocalCrdtStore();
        LocalCrdtStore crdtStore2 = new LocalCrdtStore();
        crdtStore1.connect(crdtStore2);
        ORSet<String> replica1 = crdtStore1.createORSet("ID_1");
        ORSet<String> replica2 = crdtStore2.<String>findORSet("ID_1").get();
        replica1.add("apple");
        replica2.add("banana");
        assertEquals(Set.of("apple", "banana"), new HashSet<>(replica1));
        assertEquals(Set.of("apple", "banana"), new HashSet<>(replica2));

        crdtStore1.disconnect(crdtStore2);
        replica1.remove("banana");
        replica2.add("strawberry");
        assertEquals(Set.of("apple"), new HashSet<>(replica1));
        assertEquals(Set.of("apple", "banana", "strawberry"), new HashSet<>(replica2));

        crdtStore1.connect(crdtStore2);
        assertEquals(Set.of("apple", "strawberry"), new HashSet<>(replica1));
        assertEquals(Set.of("apple", "strawberry"), new HashSet<>(replica2));

        crdtStore1.disconnect(crdtStore2);
        replica1.add("pear");
        replica2.add("pear");
        replica2.remove("pear");
        assertEquals(Set.of("apple", "strawberry", "pear"), new HashSet<>(replica1));
        assertEquals(Set.of("apple", "strawberry"), new HashSet<>(replica2));

        crdtStore1.connect(crdtStore2);
        assertEquals(Set.of("apple", "strawberry", "pear"), new HashSet<>(replica1));
        assertEquals(Set.of("apple", "strawberry", "pear"), new HashSet<>(replica2));
    }

    @Test
    public void rga() {
        LocalCrdtStore crdtStore1 = new LocalCrdtStore();
        LocalCrdtStore crdtStore2 = new LocalCrdtStore();
        crdtStore1.connect(crdtStore2);
        RGA<String> replica1 = crdtStore1.createRGA("ID_1");
        RGA<String> replica2 = crdtStore2.<String>findRGA("ID_1").get();
        replica1.add("apple");
        replica2.add("banana");
        assertEquals(Set.of("apple", "banana"), new HashSet<>(replica1));
        assertEquals(Set.of("apple", "banana"), new HashSet<>(replica2));

        crdtStore1.disconnect(crdtStore2);
        replica1.remove("banana");
        replica2.add(1, "strawberry");

        crdtStore1.connect(crdtStore2);
        assertEquals(Set.of("apple", "strawberry"), new HashSet<>(replica1));
        assertEquals(Set.of("apple", "strawberry"), new HashSet<>(replica2));

        crdtStore1.disconnect(crdtStore2);
        replica1.remove(0);
        replica1.add("pear");
        replica2.remove(0);
        replica2.add("orange");
        assertEquals(Set.of("pear", "strawberry"), new HashSet<>(replica1));
        assertEquals(Set.of("orange", "strawberry"), new HashSet<>(replica2));

        crdtStore1.connect(crdtStore2);
        assertEquals(Set.of("orange", "pear", "strawberry"), new HashSet<>(replica1));
        assertEquals(Set.of("orange", "pear", "strawberry"), new HashSet<>(replica2));
    }

}
