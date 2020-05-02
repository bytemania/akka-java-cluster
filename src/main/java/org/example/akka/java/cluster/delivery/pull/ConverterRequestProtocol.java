package org.example.akka.java.cluster.delivery.pull;

import akka.actor.typed.ActorRef;
import akka.actor.typed.delivery.WorkPullingProducerController;
import lombok.Value;

import java.util.Optional;
import java.util.UUID;

interface ConverterRequestProtocol {

    interface Command {}

    @Value(staticConstructor = "of")
    class WrappedRequestNext implements Command {
        WorkPullingProducerController.RequestNext<ImageConverter.ConversionJob> next;
    }

    @Value(staticConstructor = "of")
    class GetResult implements Command {
        UUID resultId;
        ActorRef<Optional<byte[]>> replyTo;
    }

    @Value(staticConstructor = "of")
    class ConvertRequest implements Command {
        String fromFormat;
        String toFormat;
        byte[] image;
        ActorRef<ConvertResponse> replyTo;
    }

    interface ConvertResponse {}

    @Value(staticConstructor = "of")
    class ConvertAccepted implements ConvertResponse {
        UUID resultId;
    }

    enum ConvertRejected implements ConvertResponse {
        INSTANCE
    }

    @Value(staticConstructor = "of")
    class ConvertTimedOut implements ConvertResponse {
        UUID resultId;
    }

}
