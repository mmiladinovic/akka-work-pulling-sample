package com.mmiladinovic.main;

import com.mmiladinovic.kafka.KConsumer;

/**
 * Created by miroslavmiladinovic on 16/08/15.
 */
public class KafkaTestConsumer {

    public static void main(String[] args) {
        final KConsumer consumer = new KConsumer("localhost:2181", "test1", "adimpressions", 1);
        consumer.start();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
              consumer.stop();
            }
        });


        while (true)
            consumer.read(1000);


    }
}
