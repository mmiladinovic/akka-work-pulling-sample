package com.mmiladinovic.main;

import com.mmiladinovic.aws.SQS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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
                    String msg = "message-" + UUID.randomUUID().toString();
                    sqs.sendMessages(Stream.of(msg).collect(Collectors.toList()));
                    log.info("sent message {}", msg);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0, 500, TimeUnit.MILLISECONDS);
    }

}
