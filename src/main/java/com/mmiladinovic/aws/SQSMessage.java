package com.mmiladinovic.aws;

import java.io.Serializable;

/**
 * Created by miroslavmiladinovic on 24/12/14.
 */
public class SQSMessage implements Serializable {

    public final String id;
    public final String receiptHandle;
    public final String body;
    public final long receivedAt;

    public SQSMessage(String id, String body, String receiptHandle, long receivedAt) {
        this.id = id;
        this.body = body;
        this.receiptHandle = receiptHandle;
        this.receivedAt = receivedAt;
    }

    @Override
    public String toString() {
        return "SQSMessage{" +
                "id='" + id + '\'' +
                ", receiptHandle='" + receiptHandle + '\'' +
                ", body='" + body + '\'' +
                '}';
    }
}
