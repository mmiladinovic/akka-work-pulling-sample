package com.mmiladinovic.worker;

import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.dispatch.Futures;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.pattern.Patterns;
import com.mmiladinovic.aws.SQS;
import com.mmiladinovic.aws.SQSMessage;
import scala.concurrent.Future;

/**
 * Created by miroslavmiladinovic on 29/11/2014.
 */
public class HelloWorldWorker extends Worker {

    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private final SQS sqs;

    public HelloWorldWorker(SQS sqs, ActorRef master) {
        super(master);
        this.sqs = sqs;
    }

    @Override
    public void handleWork(Object work, ActorRef workRequestor) {
        log.info("working on message {}", work);
        ActorRef messageProcessor = context().actorOf(MessageProcessor.props(sqs));
        messageProcessor.tell(new SQSMessageWithOrigin((SQSMessage) work, workRequestor), self());

        messageProcessor.tell(PoisonPill.getInstance(), ActorRef.noSender());
        // TODO sent messageProcessor a poison pill now since it will process messages sequentially?
    }

    @Override
    public void handleAny(Object work) {
        log.info("handleAny for {}", work);
        if (work instanceof MessageProcessor.MessageProcessed) {
            ActorRef deleter = context().actorOf(MessageDeleter.props(sqs));
            MessageProcessor.MessageProcessed m = (MessageProcessor.MessageProcessed) work;

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
        return Props.create(HelloWorldWorker.class, () -> new HelloWorldWorker(sqs, master));
    }
}
