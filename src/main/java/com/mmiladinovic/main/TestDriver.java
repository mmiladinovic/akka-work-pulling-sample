package com.mmiladinovic.main;

import com.mmiladinovic.aws.SQS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by miroslavmiladinovic on 28/12/14.
 */
public class TestDriver {

    private static final Logger log = LoggerFactory.getLogger(TestDriver.class);

    public static void main(String[] args) {
        final SQS sqs = new SQS(args[0]);

        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    sqs.sendMessages(generateMessages(10));
                    log.info("sent 10 messages");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0, 100, TimeUnit.MILLISECONDS);
    }

    private static List<String> generateMessages(int size) {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add("message-" + UUID.randomUUID().toString());
        }
        return list;
    }

}
