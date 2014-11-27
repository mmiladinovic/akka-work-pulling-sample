package com.mmiladinovic.master;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import com.mmiladinovic.events.WorkIsDone;
import com.mmiladinovic.events.WorkerCreated;
import com.mmiladinovic.events.WorkerRequestsWork;

/**
 * Created by miroslavmiladinovic on 27/11/2014.
 */
public class WorkMaster extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    public WorkMaster() {
        receive(ReceiveBuilder
                .match(WorkerCreated.class, this::workerCreated)
                .match(WorkerRequestsWork.class, this::workerRequestsWork)
                .match(WorkIsDone.class, this::workIsDone)
                .matchAny(o -> log.error("Unhandled event: {}", o))
                .build());
    }

    private void workerCreated(WorkerCreated msg) {

    }

    private void workerRequestsWork(WorkerRequestsWork msg) {

    }

    private void workIsDone(WorkIsDone msg) {

    }

}
