package com.mmiladinovic.worker;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import com.mmiladinovic.events.*;

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

}
