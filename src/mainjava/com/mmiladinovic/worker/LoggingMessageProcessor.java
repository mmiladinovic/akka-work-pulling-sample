package com.mmiladinovic.worker;

import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.io.Serializable;

/**
 * Created by miroslavmiladinovic on 27/12/14.
 */
public class LoggingMessageProcessor extends UntypedActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof SQSMessageWithOrigin) {
            log.info("kinda processed this message {}, responding back to sender {}", message, sender());
            sender().tell(new MessageProcessed((SQSMessageWithOrigin) message), self());
        }
        else {
            unhandled(message);
        }
    }

    public static Props props() {
        return Props.create(LoggingMessageProcessor.class, () -> new LoggingMessageProcessor());
    }

    public static final class MessageProcessed implements Serializable {
        public final SQSMessageWithOrigin messageProcessed;

        public MessageProcessed(SQSMessageWithOrigin message) {
            this.messageProcessed = message;
        }

        @Override
        public String toString() {
            return "MessageProcessed{" +
                    "messageProcessed=" + messageProcessed +
                    '}';
        }
    }
}
