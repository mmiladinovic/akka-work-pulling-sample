package com.mmiladinovic.worker;

import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.mmiladinovic.aws.SQS;
import com.mmiladinovic.aws.SQSMessage;

/**
 * Created by miroslavmiladinovic on 29/11/2014.
 */
public class MessageProcessingCoordinator extends Worker {

    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private final SQS sqs;

    public MessageProcessingCoordinator(SQS sqs, ActorRef master) {
        super(master);
        this.sqs = sqs;
    }

    @Override
    public void handleWork(Object work, ActorRef workRequestor) {
        log.debug("working on message {}", work);
        ActorRef messageProcessor = context().actorOf(LoggingMessageProcessor.props(sqs));
        messageProcessor.tell(new SQSMessageWithOrigin((SQSMessage) work, workRequestor), self());

        messageProcessor.tell(PoisonPill.getInstance(), ActorRef.noSender());
    }

    @Override
    public void handleAny(Object work) {
        log.debug("handleAny for {}", work);
        if (work instanceof LoggingMessageProcessor.MessageProcessed) {
            // TODO need to catch a timeout for if the deleter fails and ensure the worker is not stuck in "working" state
            ActorRef deleter = context().actorOf(MessageDeleter.props(sqs));
            LoggingMessageProcessor.MessageProcessed m = (LoggingMessageProcessor.MessageProcessed) work;

            deleter.tell(m.messageProcessed, self());

            deleter.tell(PoisonPill.getInstance(), ActorRef.noSender());
        }
        else if (work instanceof MessageDeleter.MessageDeleted) {
            self().tell(new WorkComplete(work), self());
        }
        else {
            unhandled(work);
        }

    }

    public static Props props(SQS sqs, ActorRef master) {
        return Props.create(MessageProcessingCoordinator.class, () -> new MessageProcessingCoordinator(sqs, master));
    }
}
