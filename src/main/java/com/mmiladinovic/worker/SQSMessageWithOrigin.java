package com.mmiladinovic.worker;

import akka.actor.ActorRef;
import com.mmiladinovic.model.AdImpression;

import java.io.Serializable;

/**
 * Created by miroslavmiladinovic on 27/12/14.
 */
public class SQSMessageWithOrigin implements Serializable {
    public final AdImpression message;
    public final ActorRef origin;

    public SQSMessageWithOrigin(AdImpression message, ActorRef origin) {
        this.message = message;
        this.origin = origin;
    }
}
