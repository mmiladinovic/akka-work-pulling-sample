package com.mmiladinovic.main;

import com.mmiladinovic.aws.SQS;
import com.mmiladinovic.metrics.MetricsRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by miroslavmiladinovic on 28/12/14.
 */
public class TestDriver {

    private static final Logger log = LoggerFactory.getLogger(TestDriver.class);

    public static void main(String[] args) {
        final SQS sqs = new SQS(args[0]);

        int threadCount = 5;
        for (int i = 0; i < threadCount; i++) {
            Thread t = new Thread(createEnqueueTask(sqs));
            t.start();
        }
    }

    private static Runnable createEnqueueTask(final SQS sqs) {
       return () -> {
           while (true) {
               try {
                   sqs.sendMessages(generateMessages(10));
                   MetricsRegistry.meterWorkEnqueued().mark(10);
               } catch (Exception e) {
                   e.printStackTrace();
               }
           }
       };
    }

    private static List<String> generateMessages(int size) {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add("message-" + UUID.randomUUID().toString());
        }
        return list;
    }

}
