package com.mmiladinovic.master;

import akka.actor.*;
import akka.dispatch.Futures;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import akka.pattern.Patterns;
import com.mmiladinovic.aws.SQS;
import com.mmiladinovic.aws.SQSMessage;
import com.mmiladinovic.message.FeedMoreWork;
import com.mmiladinovic.metrics.MetricsRegistry;
import scala.concurrent.Future;

import java.io.Serializable;
import java.util.List;

/**
 * Created by miroslavmiladinovic on 30/12/14.
 */
public class WorkFeeder extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private final SQS sqs;

    public WorkFeeder(SQS sqs) {
        this.sqs = sqs;

        receive(ReceiveBuilder
                .match(FeedMoreWork.class, this::feedMoreWork)
                .match(MessagesRead.class, this::messagesRead)
                .matchAny(o -> unhandled(o))
                .build());
    }

    private void feedMoreWork(FeedMoreWork m) {
        Future f = Futures.future(() -> new MessagesRead(sqs.readMessages(m.batchSize), m.requestor), context().dispatcher());
        Patterns.pipe(f, context().dispatcher()).to(self());
    }

    private void messagesRead(MessagesRead messages) {
        messages.messages.stream().forEach(m -> {
            MetricsRegistry.meterWorkGenerated().mark();
            messages.requestor.tell(m, self());
        });
    }

    public static Props props(SQS sqs) {
        return Props.create(WorkFeeder.class, () -> new WorkFeeder(sqs));
    }

    private static class MessagesRead implements Serializable {
        public final List<SQSMessage> messages;
        public final ActorRef requestor;

        public MessagesRead(List<SQSMessage> messages, ActorRef requestor) {
            this.messages = messages;
            this.requestor = requestor;
        }
    }

}
