package com.mmiladinovic.message;

import akka.actor.ActorRef;

import java.io.Serializable;

/**
 * Created by miroslavmiladinovic on 28/12/14.
 */
public class FeedMoreWork implements Serializable {
    public final int batchSize;
    public final ActorRef requestor;

    public FeedMoreWork(int batchSize, ActorRef requestor) {
        this.batchSize = batchSize;
        this.requestor = requestor;
    }
}
