package com.mmiladinovic.master;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import com.mmiladinovic.message.*;
import com.mmiladinovic.metrics.MetricsRegistry;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Works with a pool of workers, sending them work from the work queue
 */
public class WorkMaster extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private final Map<ActorRef, Optional<AcceptedWork>> workers = new HashMap<>();
    private final Queue<AcceptedWork> workQ = new ArrayBlockingQueue<AcceptedWork>(10000);

    public WorkMaster() {
        MetricsRegistry.registerGaugeMasterQueueDepth(workQ);

        receive(ReceiveBuilder
                .match(WorkerCreated.class, this::workerCreated)
                .match(WorkerRequestsWork.class, this::workerRequestsWork)
                .match(WorkIsDone.class, this::workIsDone)
                .match(Terminated.class, this::workerTerminated)
                .matchAny(o -> {
                    // log.info("accepting work {}", o);
                    if (workQ.offer(new AcceptedWork(sender(), o))) {
                        MetricsRegistry.meterWorkAccepted().mark();
                        notifyWorkers();
                    } else {
                        MetricsRegistry.meterWorkRejected().mark();
                        log.info("internal Q full. rejecting work {}", o);
                    }
                })
                .build());
    }

    // --  message handlers
    private void workerCreated(WorkerCreated msg) {
        // create a deathwatch on the worker
        // add to workers pool
        // notify all workers
        log.info("new worker created: {}", msg.worker);
        getContext().watch(msg.worker);
        workers.put(msg.worker, Optional.empty());
        notifyWorkers();
    }

    private void workerRequestsWork(WorkerRequestsWork msg) {
        // if we have accepted work to be done
        // and the worker is one of ours
        // and the worker is not currently busy
        // then send him WorkToBeDone msg and update the worker status
        if (workers.containsKey(msg.worker)) {
            if (workQ.isEmpty()) {
                msg.worker.tell(new NoWorkToBeDone(), self());
            }
            else if (!workers.get(msg.worker).isPresent()) {
                AcceptedWork workItem = workQ.poll();
                workers.put(msg.worker, Optional.of(workItem));
                msg.worker.tell(new WorkToBeDone(workItem.work), workItem.requestor);
            }
        }
    }

    private void workIsDone(WorkIsDone msg) {
        // if we know about this worker, then set its status to Optional.empty()
        if (workers.containsKey(msg.worker)) {
            workers.put(msg.worker, Optional.empty());
        }
        else {
            log.error("actor {} is reporting work is done but I don't know about him.", msg.worker);
        }
    }

    private void workerTerminated(Terminated terminated) {
        // check if its still one of our actors
        // and if so remove him from the pool but first re-assign the work through the inputQ
        ActorRef dead = terminated.actor();
        if (workers.containsKey(dead)) {
            if (workers.get(dead).isPresent()) {
                AcceptedWork work = workers.get(dead).get();
                self().tell(work.work, work.requestor);
            }
            workers.remove(dead);
        }
        else {
            log.error("Termination message came from actor we don't know about: {}", terminated.actor());
        }
    }


    private void notifyWorkers() {
        // send WorkIsReady to all available and non busy workers
        if (!workQ.isEmpty()) {
            workers.keySet().stream().
                    filter(worker -> !workers.get(worker).isPresent()).
                    forEach(worker -> worker.tell(new WorkIsReady(), self()));
        }
    }

    // actor creation
    public static Props props() {
        return Props.create(WorkMaster.class, () -> new WorkMaster());
    }

    private static final class AcceptedWork {
        public final ActorRef requestor;
        public final Object work;

        public AcceptedWork(ActorRef requestor, Object work) {
            this.requestor = requestor;
            this.work = work;
        }
    }

}
