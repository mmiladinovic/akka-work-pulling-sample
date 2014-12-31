package com.mmiladinovic.worker;

import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.mmiladinovic.aws.SQS;
import com.mmiladinovic.metrics.MetricsRegistry;

import java.io.Serializable;

/**
 * Created by miroslavmiladinovic on 27/12/14.
 */
public class MessageDeleter extends UntypedActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private final SQS sqs;

    public MessageDeleter(SQS sqs) {
        this.sqs = sqs;
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof SQSMessageWithOrigin) {
            SQSMessageWithOrigin sqsMessage = (SQSMessageWithOrigin) message;
            sqs.deleteMessage(sqsMessage.message);
            log.debug("deleted message {}", message);
            MetricsRegistry.histogramProcessingLatency().update(System.currentTimeMillis() - sqsMessage.message.receivedAt);
            sender().tell(new MessageDeleted(sqsMessage), self());
        }
        else {
            unhandled(message);
        }
    }

    @Override
    public void postStop() throws Exception {
        super.postStop();
        log.info("message deleter {} stopping.", self());
    }

    public static Props props(SQS sqs) {
        return Props.create(MessageDeleter.class, () -> new MessageDeleter(sqs));
    }

    public static final class MessageDeleted implements Serializable {
        public final SQSMessageWithOrigin messageDeleted;

        public MessageDeleted(SQSMessageWithOrigin messageDeleted) {
            this.messageDeleted = messageDeleted;
        }
    }
}
