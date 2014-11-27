package com.mmiladinovic.message;

import akka.actor.ActorRef;

import java.io.Serializable;

/**
 * Created by miroslavmiladinovic on 27/11/2014.
 */
public class WorkIsDone implements Serializable {

    public final ActorRef worker;

    public WorkIsDone(ActorRef worker) {
        this.worker = worker;
    }
}
