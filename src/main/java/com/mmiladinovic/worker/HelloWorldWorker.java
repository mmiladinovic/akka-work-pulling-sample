package com.mmiladinovic.worker;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.dispatch.Futures;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.pattern.Patterns;
import scala.concurrent.Future;

/**
 * Created by miroslavmiladinovic on 29/11/2014.
 */
public class HelloWorldWorker extends Worker {

    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    public HelloWorldWorker(ActorRef master) {
        super(master);
    }

    @Override
    public Object handleWork(Object work, ActorRef workRequestor) {
        final String helloSaid = "hello said to: "+work;
        Future f = Futures.future(() -> {
            log.info("sayin {}", helloSaid);
            return new WorkComplete(helloSaid);

        }, context().dispatcher());
        Patterns.pipe(f, context().dispatcher()).to(self());

        return helloSaid;
    }

    public static Props props(ActorRef master) {
        return Props.create(HelloWorldWorker.class, () -> new HelloWorldWorker(master));
    }
}
