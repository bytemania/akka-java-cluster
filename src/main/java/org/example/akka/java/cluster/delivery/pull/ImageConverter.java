package org.example.akka.java.cluster.delivery.pull;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.delivery.ConsumerController;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.receptionist.ServiceKey;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.UUID;

@Slf4j
class ImageConverter {

    interface InternalCommand extends ImageConverterProtocol.Command {}

    @Value(staticConstructor = "of")
    static class ConversionJob {
        UUID resultId;
        String fromFormat;
        String toFormat;
        byte[] image;
    }

    @Value(staticConstructor = "of")
    private static class WrappedDelivery implements InternalCommand {
        ConsumerController.Delivery<ConversionJob> delivery;
    }

    public static final ServiceKey<ConsumerController.Command<ConversionJob>> serviceKey =
            ServiceKey.create(ConsumerController.serviceKeyClass(), "ImageConverter");

    public static Behavior<ImageConverterProtocol.Command> create() {
        return Behaviors.setup(context -> {
            ActorRef<ConsumerController.Delivery<ConversionJob>> deliveryAdapter =
                    context.messageAdapter(ConsumerController.deliveryClass(), WrappedDelivery::new);

            ActorRef<ConsumerController.Command<ConversionJob>> consumerController =
                    context.spawn(ConsumerController.create(serviceKey), "consumerController");

            consumerController.tell(new ConsumerController.Start<>(deliveryAdapter));

            return Behaviors.receive(ImageConverterProtocol.Command.class)
                    .onMessage(WrappedDelivery.class, ImageConverter::onDelivery)
                    .build();
        });
    }

    private static Behavior<ImageConverterProtocol.Command> onDelivery(WrappedDelivery w) {
        byte[] image = w.delivery.message().image;
        String fromFormat = w.delivery.message().fromFormat;
        String toFormat = w.delivery.message().toFormat;
        log.info("CONVERSION {}-{}-{}", fromFormat, toFormat, Arrays.toString(image));
        w.delivery.confirmTo().tell(ConsumerController.confirmed());
        return Behaviors.same();
    }
}
