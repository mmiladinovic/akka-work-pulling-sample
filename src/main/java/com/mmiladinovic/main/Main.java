package com.mmiladinovic.main;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.mmiladinovic.aws.SQS;
import com.mmiladinovic.master.WorkFeeder;
import com.mmiladinovic.master.WorkMaster;
import com.mmiladinovic.worker.MessageProcessingCoordinator;

/**
 * Created by miroslavmiladinovic on 29/11/2014.
 */
public class Main {

    public static void main(String args[]) {
        String queueUrl = args[0];
        int noOfWorkers = args.length > 1 ? Integer.parseInt(args[1]) : 10;

        ActorSystem system = ActorSystem.create("akka-work-pulling-hello");

        ActorRef workPullingActors = system.actorOf(WorkPullingActors.props(queueUrl, noOfWorkers), "work-pulling-sample");
    }
}
