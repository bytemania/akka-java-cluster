package org.example.akka.java.cluster.delivery.pull;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.delivery.WorkPullingProducerController;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.StashBuffer;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
class ImageWorkManager {

    interface InternalCommand extends ImageWorkManagerProtocol.Command {}

    @Value(staticConstructor = "of")
    private static class WrappedRequestNext implements InternalCommand {
        WorkPullingProducerController.RequestNext<ImageConverter.ConversionJob> next;
    }

    public static Behavior<ImageWorkManagerProtocol.Command> create() {
        return Behaviors.setup(context -> {
            ActorRef<WorkPullingProducerController.RequestNext<ImageConverter.ConversionJob>> requestNextAdapter =
                    context.messageAdapter(WorkPullingProducerController.requestNextClass(), WrappedRequestNext::of);

            ActorRef<WorkPullingProducerController.Command<ImageConverter.ConversionJob>> producerController =
                    context.spawn(WorkPullingProducerController.create(ImageConverter.ConversionJob.class, "workManager",
                            ImageConverter.serviceKey, Optional.empty()), "producerController");

            producerController.tell(new WorkPullingProducerController.Start<>(requestNextAdapter));

            return Behaviors.withStash(1000, stashBuffer -> new ImageWorkManager(context, stashBuffer).waitForNext());
        });
    }

    private final ActorContext<ImageWorkManagerProtocol.Command> context;
    private final StashBuffer<ImageWorkManagerProtocol.Command> stashBuffer;

    private Behavior<ImageWorkManagerProtocol.Command> waitForNext() {
        return Behaviors.receive(ImageWorkManagerProtocol.Command.class)
                .onMessage(WrappedRequestNext.class, this::onWrappedRequestNext)
                .onMessage(ImageWorkManagerProtocol.Convert.class, this::onConvertWait)
                .onMessage(ImageWorkManagerProtocol.GetResult.class, this::onGetResult)
                .build();
    }

    private Behavior<ImageWorkManagerProtocol.Command> active(WorkPullingProducerController.RequestNext<ImageConverter.ConversionJob> next) {
        return Behaviors.receive(ImageWorkManagerProtocol.Command.class)
                .onMessage(ImageWorkManagerProtocol.Convert.class, c -> onConvert(c, next))
                .onMessage(ImageWorkManagerProtocol.GetResult.class, this::onGetResult)
                .onMessage(WrappedRequestNext.class, this::onUnexpectedWrappedRequestNext)
                .build();
    }

    private Behavior<ImageWorkManagerProtocol.Command> onWrappedRequestNext(WrappedRequestNext w) {
        return stashBuffer.unstashAll(active(w.next));
    }

    private Behavior<ImageWorkManagerProtocol.Command> onConvertWait(ImageWorkManagerProtocol.Convert convert) {
        if (stashBuffer.isFull()) {
            context.getLog().warn("Too many Convert requests.");
        } else {
            stashBuffer.stash(convert);
        }
        return Behaviors.same();
    }

    private Behavior<ImageWorkManagerProtocol.Command> onUnexpectedWrappedRequestNext(WrappedRequestNext w) {
        throw new IllegalStateException("Unexpected RequestNext");
    }

    private Behavior<ImageWorkManagerProtocol.Command> onGetResult(ImageWorkManagerProtocol.GetResult get) {
        return Behaviors.same();
    }

    private Behavior<ImageWorkManagerProtocol.Command> onConvert(ImageWorkManagerProtocol.Convert convert,
                                                                 WorkPullingProducerController.RequestNext<ImageConverter.ConversionJob> next) {
        UUID resultId = UUID.randomUUID();
        next.sendNextTo().tell(ImageConverter.ConversionJob.of(resultId, convert.getFromFormat(), convert.getToFormat(), convert.getImage()));
        return waitForNext();
    }
}
