package org.example.akka.java.cluster.ddata;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.cluster.ddata.GCounter;
import akka.cluster.ddata.Key;
import akka.cluster.ddata.SelfUniqueAddress;
import akka.cluster.ddata.typed.javadsl.DistributedData;
import akka.cluster.ddata.typed.javadsl.Replicator;
import akka.cluster.ddata.typed.javadsl.ReplicatorMessageAdapter;

class Counter extends AbstractBehavior<CounterProtocol.Command> {

    public static Behavior<CounterProtocol.Command> create(Key<GCounter> key) {
        return Behaviors.setup(ctx -> DistributedData.withReplicatorMessageAdapter(
                (ReplicatorMessageAdapter<CounterProtocol.Command, GCounter> replicatorAdapter) ->
                        new Counter(ctx, replicatorAdapter, key)) );
    }

    private final Key<GCounter> key;
    private final ReplicatorMessageAdapter<CounterProtocol.Command, GCounter> replicatorAdapter;
    private final SelfUniqueAddress node;
    private int cachedValue;

    private Counter(ActorContext<CounterProtocol.Command> context,
                    ReplicatorMessageAdapter<CounterProtocol.Command, GCounter> replicatorAdapter,
                    Key<GCounter> key) {
        super(context);

        this.key = key;

        this.replicatorAdapter = replicatorAdapter;
        this.replicatorAdapter.subscribe(this.key, CounterInternalProtocol.InternalSubscribeResponse::of);

        this.node = DistributedData.get(context.getSystem()).selfUniqueAddress();

        this.cachedValue = 0;
    }

    @Override
    public Receive<CounterProtocol.Command> createReceive() {
        return newReceiveBuilder()
                .onMessageEquals(CounterProtocol.Increment.INSTANCE, this::onIncrement)
                .onMessage(CounterInternalProtocol.InternalUpdateResponse.class, notUsed -> Behaviors.same())
                .onMessage(CounterProtocol.GetValue.class, this::onGetValue)
                .onMessage(CounterProtocol.GetCachedValue.class, this::onGetCachedValue)
                .onMessageEquals(CounterProtocol.Unsubscribe.INSTANCE, this::onUnsubscribe)
                .onMessage(CounterInternalProtocol.InternalGetResponse.class, this::onInternalGetResponse)
                .onMessage(CounterInternalProtocol.InternalSubscribeResponse.class, this::onInternalSubscribeResponse)
                .build();
    }

    private Behavior<CounterProtocol.Command> onIncrement() {
        replicatorAdapter.askUpdate(askReplyTo ->
                        new Replicator.Update<>(key, GCounter.empty(), Replicator.writeLocal(), askReplyTo,
                                curr -> curr.increment(node, 1)),
                CounterInternalProtocol.InternalUpdateResponse::of);
        return this;
    }

    private Behavior<CounterProtocol.Command> onGetValue(CounterProtocol.GetValue cmd) {
        replicatorAdapter.askGet(
                askReplyTo -> new Replicator.Get<>(key, Replicator.readLocal(), askReplyTo),
                rsp -> CounterInternalProtocol.InternalGetResponse.of(rsp, cmd.getReplyTo())
        );
        return this;
    }

    private Behavior<CounterProtocol.Command> onGetCachedValue(CounterProtocol.GetCachedValue cmd) {
        cmd.getReplyTo().tell(cachedValue);
        return this;
    }

    private Behavior<CounterProtocol.Command> onUnsubscribe() {
        replicatorAdapter.unsubscribe(key);
        return this;
    }

    private Behavior<CounterProtocol.Command> onInternalGetResponse(CounterInternalProtocol.InternalGetResponse msg) {
        if (msg.getRsp() instanceof Replicator.GetSuccess) {
            int value = ((Replicator.GetSuccess<?>) msg.getRsp()).get(key).getValue().intValue();
            msg.getReplyTo().tell(value);
            return this;
        } else {
            return Behaviors.unhandled();
        }
    }

    private Behavior<CounterProtocol.Command> onInternalSubscribeResponse(CounterInternalProtocol.InternalSubscribeResponse msg) {
        if (msg.getRsp() instanceof Replicator.Changed) {
            GCounter counter = ((Replicator.Changed<?>) msg.getRsp()).get(key);
            cachedValue = counter.getValue().intValue();
            return this;
        } else {
            return Behaviors.unhandled();
        }
    }
}
