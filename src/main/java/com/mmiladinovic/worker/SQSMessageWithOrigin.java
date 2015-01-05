package com.mmiladinovic.worker;

import akka.actor.ActorRef;
import com.mmiladinovic.aws.SQSMessage;

import java.io.Serializable;

/**
 * Created by miroslavmiladinovic on 27/12/14.
 */
public class SQSMessageWithOrigin implements Serializable {
    public final SQSMessage message;
    public final ActorRef origin;

    public SQSMessageWithOrigin(SQSMessage message, ActorRef origin) {
        this.message = message;
        this.origin = origin;
    }
}
