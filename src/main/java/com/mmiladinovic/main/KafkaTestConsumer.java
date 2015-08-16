package com.mmiladinovic.main;

import com.mmiladinovic.kafka.KConsumer;
import com.mmiladinovic.metrics.MetricsRegistry;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * Created by miroslavmiladinovic on 16/08/15.
 */
public class KafkaTestConsumer {

    public static void main(String[] args) {
        final KConsumer consumer = new KConsumer("localhost:2181", "miro", "adimpressions", 1);
        consumer.start();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
              consumer.stop();
            }
        });


        while (true) {
            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(50));
            MetricsRegistry.meterWorkDequeued().mark(consumer.read(1000).size());
            consumer.commitBatch();
        }


    }
}
