package com.mmiladinovic.main;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.mmiladinovic.kafka.KConsumer;
import com.mmiladinovic.master.WorkFeeder;
import com.mmiladinovic.master.WorkMaster;
import com.mmiladinovic.worker.MessageProcessingCoordinator;

/**
 * Created by miroslavmiladinovic on 30/12/14.
 */
public class WorkPullingActors extends UntypedActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private final String queueUrl;
    private final int workerCount;
    private KConsumer sqs;

    private ActorRef feeder;
    private ActorRef master;

    public WorkPullingActors(String queueUrl, int workerCount) {
        this.queueUrl = queueUrl;
        this.workerCount = workerCount;
    }

    @Override
    public void preStart() throws Exception {
        sqs = new KConsumer(queueUrl, "akka-test", "adimpressions", 1);

        feeder = context().actorOf(WorkFeeder.props(sqs), "work-feeder");
        master = context().actorOf(WorkMaster.props(feeder), "work-master");
        for (int i = 0; i < workerCount; i++) {
            context().actorOf(MessageProcessingCoordinator.props(master), "worker-" + i);
        }

        sqs.start();

    }

    @Override
    public void onReceive(Object message) throws Exception {
        log.warning("received message {} but no supervision logic set up yet.");
        unhandled(message);
    }

    public static Props props(String queueUrl, int workerCount) {
        return Props.create(WorkPullingActors.class, () -> new WorkPullingActors(queueUrl, workerCount));
    }

}
