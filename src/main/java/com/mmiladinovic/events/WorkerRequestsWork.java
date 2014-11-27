package com.mmiladinovic.events;

import akka.actor.ActorRef;

import java.io.Serializable;

/**
 * Created by miroslavmiladinovic on 27/11/2014.
 */
public class WorkerRequestsWork implements Serializable {

    public final ActorRef worker;

    public WorkerRequestsWork(ActorRef worker) {
        this.worker = worker;
    }
}
