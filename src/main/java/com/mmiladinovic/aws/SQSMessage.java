package com.mmiladinovic.aws;

import java.io.Serializable;

/**
 * Created by miroslavmiladinovic on 24/12/14.
 */
public class SQSMessage implements Serializable {

    public final String id;
    public final String receiptHandle;
    public final String body;

    public SQSMessage(String id, String body, String receiptHandle) {
        this.id = id;
        this.body = body;
        this.receiptHandle = receiptHandle;
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
