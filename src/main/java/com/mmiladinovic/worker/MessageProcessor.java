package com.mmiladinovic.worker;

import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.mmiladinovic.aws.SQS;
import com.mmiladinovic.aws.SQSMessage;

import java.io.Serializable;

/**
 * Created by miroslavmiladinovic on 27/12/14.
 */
public class MessageProcessor extends UntypedActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private final SQS sqs;

    public MessageProcessor(SQS sqs) {
        this.sqs = sqs;
    }

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

    public static Props props(SQS sqs) {
        return Props.create(MessageProcessor.class, () -> new MessageProcessor(sqs));
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
