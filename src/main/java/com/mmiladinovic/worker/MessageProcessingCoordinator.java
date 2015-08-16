package com.mmiladinovic.worker;

import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.mmiladinovic.model.AdImpression;

/**
 * Created by miroslavmiladinovic on 29/11/2014.
 */
public class MessageProcessingCoordinator extends Worker {

    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    public MessageProcessingCoordinator(ActorRef master) {
        super(master);
    }

    @Override
    public void handleWork(Object work, ActorRef workRequestor) {
        log.debug("working on message {}", work);
        ActorRef messageProcessor = context().actorOf(LoggingMessageProcessor.props());
        messageProcessor.tell(new SQSMessageWithOrigin((AdImpression) work, workRequestor), self());

        messageProcessor.tell(PoisonPill.getInstance(), ActorRef.noSender());
    }

    @Override
    public void handleAny(Object work) {
        log.debug("handleAny for {}", work);
        if (work instanceof LoggingMessageProcessor.MessageProcessed) {
            // TODO need to catch a timeout for if the deleter fails and ensure the worker is not stuck in "working" state
            self().tell(new WorkComplete(work), self());
        }
        else {
            unhandled(work);
        }

    }

    public static Props props(ActorRef master) {
        return Props.create(MessageProcessingCoordinator.class, () -> new MessageProcessingCoordinator(master));
    }
}
