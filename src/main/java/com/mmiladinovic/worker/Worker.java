package com.mmiladinovic.worker;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import com.mmiladinovic.message.NoWorkToBeDone;
import com.mmiladinovic.message.WorkIsReady;
import com.mmiladinovic.message.WorkToBeDone;

/**
 * Created by miroslavmiladinovic on 27/11/2014.
 */
public class Worker extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    public Worker() {
        receive(ReceiveBuilder
                .match(WorkToBeDone.class, this::workToBeDone)
                .match(WorkIsReady.class, this::workIsReady)
                .match(NoWorkToBeDone.class, this::noWorkToBeDone)
                .matchAny(o -> log.error("Unhandled event: {}", o))
                .build());
    }

    private void workToBeDone(WorkToBeDone msg) {

    }

    private void workIsReady(WorkIsReady msg) {

    }

    private void noWorkToBeDone(NoWorkToBeDone msg) {

    }

    public static Props props() {
        return Props.create(Worker.class, () -> new Worker());
    }

}
