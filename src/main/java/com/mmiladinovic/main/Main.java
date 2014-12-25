package com.mmiladinovic.main;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.mmiladinovic.master.WorkMaster;
import com.mmiladinovic.metrics.MetricsRegistry;
import com.mmiladinovic.worker.HelloWorldWorker;
import scala.concurrent.duration.Duration;

import java.util.UUID;

/**
 * Created by miroslavmiladinovic on 29/11/2014.
 */
public class Main {

    public static void main(String args[]) {

        ActorSystem system = ActorSystem.create("akka-work-pulling-hello");

        ActorRef master = system.actorOf(WorkMaster.props(), "master");
        for (int i = 0; i < 5; i++) {
            system.actorOf(HelloWorldWorker.props(master), "worker-"+i);
        }

        system.scheduler().scheduleOnce(
                Duration.Zero(), (Runnable) () -> {
                    for (int i = 1; true; i++) {
                        if ((i % 1000) == 0) {
                            MetricsRegistry.meterWorkGenerated().mark(1000);
                            i = 1;
                        }
                        master.tell(UUID.randomUUID().toString(), ActorRef.noSender());
                    }
                },
                system.dispatcher());
    }
}
