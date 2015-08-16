package com.mmiladinovic.main;

import com.mmiladinovic.kafka.KProducer;
import com.mmiladinovic.metrics.MetricsRegistry;
import com.mmiladinovic.model.AdImpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * Created by miroslavmiladinovic on 16/08/15.
 */
public class KafkaTestProducer {

    private static final Logger log = LoggerFactory.getLogger(KafkaTestProducer.class);

    public static void main(String[] args) {
        KProducer producer = new KProducer("localhost:9092", "adimpressions");
        final SenderTask t = new SenderTask(producer);


        ExecutorService e = Executors.newFixedThreadPool(4);
        e.submit(t);

        final Thread mainThread = Thread.currentThread();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                log.info("Shutting down...");
                t.shutdown();

                try {
                    e.shutdown();
                    e.awaitTermination(500, TimeUnit.MILLISECONDS);

                } catch (InterruptedException e1) {
                    log.error("interrupt while shutting down", e1);
                }
                log.info("Shutdown complete...");
            }
        });

        try {
            mainThread.join();
            log.info("Exiting.");
        } catch (InterruptedException e1) {
            log.error("interrupt while shutting down", e1);
        }

    }

    private static class SenderTask implements Runnable {
        private final Random r = new Random();
        private final String[] COUNTRIES = new String[] {"GB", "DE", "RU", "US", "FR", "IT", "ES", "CN", "JP"};

        private volatile boolean shutdown = false;

        private final KProducer producer;

        public SenderTask(KProducer producer) {
            this.producer = producer;
        }

        @Override
        public void run() {
            while (!shutdown) {
                // LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(50));
                producer.send(adImp());
                MetricsRegistry.meterWorkEnqueued().mark();
            }
        }

        public void shutdown() {
            this.shutdown = true;
        }

        private AdImpression adImp() {
            return new AdImpression(
                    new Long(r.nextInt(10000)),
                    COUNTRIES[r.nextInt(COUNTRIES.length)],
                    new Long(r.nextInt(100000)));
        }
    }

}
