package com.mmiladinovic.main;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.mmiladinovic.aws.SQS;
import com.mmiladinovic.aws.SQSMessage;
import com.mmiladinovic.master.WorkMaster;
import com.mmiladinovic.metrics.MetricsRegistry;
import com.mmiladinovic.worker.HelloWorldWorker;
import scala.concurrent.duration.Duration;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by miroslavmiladinovic on 29/11/2014.
 */
public class Main {

    public static void main(String args[]) {
        SQS sqs = new SQS(args[0]);

        ActorSystem system = ActorSystem.create("akka-work-pulling-hello");

        ActorRef master = system.actorOf(WorkMaster.props(), "master");
        for (int i = 0; i < 5; i++) {
            system.actorOf(HelloWorldWorker.props(sqs, master), "worker-"+i);
        }

        system.scheduler().schedule(
                Duration.create(1, TimeUnit.SECONDS),
                Duration.create(1, TimeUnit.SECONDS), (Runnable) () -> {
                    sqs.readMessages(10).stream().forEach(m -> {
                        master.tell(m, ActorRef.noSender());
                        MetricsRegistry.meterWorkGenerated().mark();
                    });
                },
                system.dispatcher());
    }
}
