package com.mmiladinovic.worker;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import com.mmiladinovic.message.*;
import com.mmiladinovic.metrics.MetricsRegistry;
import scala.PartialFunction;
import scala.runtime.BoxedUnit;

import java.io.Serializable;

/**
 * Created by miroslavmiladinovic on 27/11/2014.
 */
public abstract class Worker extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private final ActorRef master;

    private final PartialFunction<Object, BoxedUnit> working;
    private final PartialFunction<Object, BoxedUnit> idle;

    public Worker(String masterActorPath) {
        master = context().actorFor(masterActorPath);

        working = ReceiveBuilder.
                match(WorkComplete.class, this::workComplete).
                match(WorkToBeDone.class, m -> {
                    log.error("I shouldn't be asked to work whilst already working");
                }).
                match(NoWorkToBeDone.class, m -> {/* we asked for work but there's none. ignore */}).
                matchAny(m -> {
                    log.error("unhandled message whilst in working state: {}", m);
                }).
                build();

        idle = ReceiveBuilder.
                match(WorkToBeDone.class, this::workToBeDone).
                match(WorkIsReady.class, this::workIsReady).
                match(NoWorkToBeDone.class, m -> {/* we shouldn't really be getting this in idle state */}).
                matchAny(m -> {
                    log.error("unhandled message whilst in idle state", m);
                }).
                build();

        receive(idle); // start from idle state
    }

    @Override
    public void preStart() throws Exception {
        master.tell(new WorkerCreated(self()), self());
    }

    private void workToBeDone(WorkToBeDone msg) {
        // invoke handleWork
        log.info("Received work to do {}", msg.work);
        context().become(working);
        handleWork(msg.work, sender()); // TODO this not the other way round for sync work handlers?
    }

    private void workIsReady(WorkIsReady msg) {
        log.info("requesting work");
        master.tell(new WorkerRequestsWork(self()), self());
    }

    private void workComplete(WorkComplete msg) {
        log.info("work is complete {}", msg.work);
        MetricsRegistry.meterWorkCompleted().mark();

        master.tell(new WorkIsDone(self()), self());
        master.tell(new WorkerRequestsWork(self()), self());
        context().become(idle);
    }

    public abstract Object handleWork(Object work, ActorRef workRequestor);

    // -- private messages
    public static class WorkComplete implements Serializable {
        public final Object work;

        public WorkComplete(Object work) {
            this.work = work;
        }
    }
}
