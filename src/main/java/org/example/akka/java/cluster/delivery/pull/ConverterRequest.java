package org.example.akka.java.cluster.delivery.pull;

import akka.Done;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.delivery.WorkPullingProducerController;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.StashBuffer;
import lombok.Value;

import java.time.Duration;
import java.util.UUID;

class ConverterRequest {

    interface InternalCommand extends ConverterRequestProtocol.Command {}

    @Value(staticConstructor = "of")
    private static class AskReply implements InternalCommand {
        UUID resultId;
        ActorRef<ConverterRequestProtocol.ConvertResponse> originalReplyTo;
        boolean timeout;
    }

    private final ActorContext<ConverterRequestProtocol.Command> context;
    private final StashBuffer<ConverterRequestProtocol.Command> stashBuffer;

    private ConverterRequest(ActorContext<ConverterRequestProtocol.Command> context,
                             StashBuffer<ConverterRequestProtocol.Command> stashBuffer) {
        this.context = context;
        this.stashBuffer = stashBuffer;
    }

    private Behavior<ConverterRequestProtocol.Command> waitForNext() {
        return Behaviors.receive(ConverterRequestProtocol.Command.class)
                .onMessage(ConverterRequestProtocol.WrappedRequestNext.class, this::onWrappedRequestNext)
                .onMessage(ConverterRequestProtocol.ConvertRequest.class, this::onConvertRequestWait)
                .onMessage(AskReply.class, this::onAskReply)
                .onMessage(ConverterRequestProtocol.GetResult.class, this::onGetResult)
                .build();
    }

    private Behavior<ConverterRequestProtocol.Command> active(WorkPullingProducerController.RequestNext<ImageConverter.ConversionJob> next) {
        return Behaviors.receive(ConverterRequestProtocol.Command.class)
                .onMessage(ConverterRequestProtocol.ConvertRequest.class, c -> onConvertRequest(c, next))
                .onMessage(AskReply.class, this::onAskReply)
                .onMessage(ConverterRequestProtocol.GetResult.class, this::onGetResult)
                .onMessage(ConverterRequestProtocol.WrappedRequestNext.class, this::onUnexpectedWrappedRequestNext)
                .build();
    }

    private Behavior<ConverterRequestProtocol.Command> onWrappedRequestNext(ConverterRequestProtocol.WrappedRequestNext w) {
        return stashBuffer.unstashAll(active(w.getNext()));
    }

    private Behavior<ConverterRequestProtocol.Command> onConvertRequestWait(ConverterRequestProtocol.ConvertRequest convert) {
        if (stashBuffer.isFull()) {
            convert.getReplyTo().tell(ConverterRequestProtocol.ConvertRejected.INSTANCE);
        } else {
            stashBuffer.stash(convert);
        }
        return Behaviors.same();
    }

    private Behavior<ConverterRequestProtocol.Command> onAskReply(AskReply reply) {
        if (reply.timeout) reply.originalReplyTo.tell(ConverterRequestProtocol.ConvertTimedOut.of(reply.resultId));
        else reply.originalReplyTo.tell(ConverterRequestProtocol.ConvertAccepted.of(reply.resultId));
        return Behaviors.same();
    }

    private Behavior<ConverterRequestProtocol.Command> onGetResult(ConverterRequestProtocol.GetResult get) {
        return Behaviors.same();
    }

    private Behavior<ConverterRequestProtocol.Command> onConvertRequest(ConverterRequestProtocol.ConvertRequest convert,
                                                                        WorkPullingProducerController.RequestNext<ImageConverter.ConversionJob> next) {
        UUID resultId = UUID.randomUUID();
        context.ask(
                Done.class,
                next.askNextTo(),
                Duration.ofSeconds(5),
                askReplyTo -> new WorkPullingProducerController.MessageWithConfirmation<>(
                        ImageConverter.ConversionJob.of(resultId, convert.getFromFormat(), convert.getToFormat(), convert.getImage()), askReplyTo),
                (done, exc) -> {
                    if (exc == null) return new AskReply(resultId, convert.getReplyTo(), false);
                    else return new AskReply(resultId, convert.getReplyTo(), true);
                });

        return waitForNext();
    }

    private Behavior<ConverterRequestProtocol.Command> onUnexpectedWrappedRequestNext(ConverterRequestProtocol.WrappedRequestNext w) {
        throw new IllegalStateException("Unexpected RequestNext");
    }

}
