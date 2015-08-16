package com.mmiladinovic.kafka;

import com.codahale.metrics.*;
import com.codahale.metrics.Timer;
import com.google.gson.Gson;
import com.mmiladinovic.metrics.MetricsRegistry;
import com.mmiladinovic.model.AdImpression;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;

/**
 * Created by miroslavmiladinovic on 16/08/15.
 */
public class KConsumer {

    private static final Logger log = LoggerFactory.getLogger(KConsumer.class);

    public static final int MAX_BATCH_SIZE = 10000;

    private final String zkUrl;
    private final String topic;
    private final String groupId;
    private final int consumerThreads;

    private final ConsumerConnector consumer;
    private final LinkedBlockingQueue<AdImpression> queue = new LinkedBlockingQueue<>(100000);


    private final ExecutorService e;

    private final List<KafkaStream<byte[], byte[]>> kafkaStreams;
    private final List<ConsumerThread> threads;

    public KConsumer(String zkUrl, String groupId, String topic, int consumerThreads) {
        this.zkUrl = zkUrl;
        this.topic = topic;
        this.groupId = groupId;
        this.consumerThreads = consumerThreads;

        this.consumer = kafka.consumer.Consumer.createJavaConsumerConnector(
                createConsumerConfig(zkUrl, groupId));

        this.e = Executors.newFixedThreadPool(consumerThreads);

        Map<String, Integer> topicCountMap = new HashMap<>();
        topicCountMap.put(topic, new Integer(consumerThreads));
        Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumer.createMessageStreams(topicCountMap);
        kafkaStreams = consumerMap.get(topic);

        threads = new ArrayList<>(consumerThreads);
        for (KafkaStream s : kafkaStreams) {
            threads.add(new ConsumerThread(s, queue));
        }

        MetricsRegistry.registerKafkaConsumerQueueDepth(queue);
    }

    public void start() {
        for (ConsumerThread t : threads) {
            e.submit(t);
        }
        log.info("Kafka consumer threads started: {}", consumerThreads);
    }

    public void stop() {
        log.info("Stopping Kafka consumer threads");


        for (ConsumerThread t : threads) {
            t.shutdown();
        }
        consumer.shutdown();

        e.shutdown();
        try {
            e.awaitTermination(500, TimeUnit.MILLISECONDS);
            log.info("Kafka consumer threads stop completed ok.");
        } catch (InterruptedException e1) {
            log.warn("interrupt while awaiting for termination", e1);
        }
    }

    public List<AdImpression> read() {
        return read(100);
    }

    public List<AdImpression> read(int batchSize) {
        if (batchSize < 0 || MAX_BATCH_SIZE > 100000) throw new IllegalArgumentException("batch size out of bounds");

        List<AdImpression> retval = new ArrayList<>(batchSize);
        try {
            retval.add(queue.take()); // block to avoid busy wait
        } catch (InterruptedException e1) {
            log.warn("interrupt while waiting for buffer to fill in");
        }
        queue.drainTo(retval, batchSize - 1);

        return retval;
    }

    public List<AdImpression> read(int batchSize, long timeout, TimeUnit unit) {
        if (batchSize < 0 || MAX_BATCH_SIZE > 100000) throw new IllegalArgumentException("batch size out of bounds");

        List<AdImpression> retval = new ArrayList<>(batchSize);
        AdImpression imp = null;
        try {
            imp = queue.poll(timeout, unit);
        } catch (InterruptedException e1) {
            log.warn("interrupt while waiting for buffer to fill in");
        }
        if (imp != null) {
            retval.add(imp);
            queue.drainTo(retval, batchSize - 1);
        }

        return retval;
    }


    public void commitBatch() {
        this.consumer.commitOffsets();
    }

    private static ConsumerConfig createConsumerConfig(String a_zookeeper, String a_groupId) {
        Properties props = new Properties();
        props.put("zookeeper.connect", a_zookeeper);
        props.put("group.id", a_groupId);
        props.put("zookeeper.session.timeout.ms", "400");
        props.put("zookeeper.sync.time.ms", "200");
        props.put("auto.commit.interval.ms", "1000");
        props.put("auto.offset.reset", "smallest");
        props.put("auto.commit.enable", "false");

        return new ConsumerConfig(props);
    }


    private static final class ConsumerThread implements Runnable {
        private volatile boolean shutdown = false;

        private final ConsumerIterator<byte[], byte[]> kafkaStream;
        private final BlockingQueue<AdImpression> queue;

        private final Gson gson = new Gson();

        public ConsumerThread(KafkaStream<byte[], byte[]> kafkaStream, BlockingQueue<AdImpression> queue) {
            this.kafkaStream = kafkaStream.iterator();
            this.queue = queue;
        }

        @Override
        public void run() {
            while (!shutdown && kafkaStream.hasNext()) {

                byte[] message = null;
                Timer.Context time = MetricsRegistry.timerKafkaRead().time();
                try {
                     message = kafkaStream.next().message();
                }
                finally {
                    time.stop();
                }

                final AdImpression imp = gson.fromJson(new String(message), AdImpression.class);
                try {
                    queue.put(imp);
                } catch (InterruptedException e1) {
                    log.warn("interrupt while waiting for queue to gain capacity", e1);
                }
            }
        }

        public void shutdown() {
            this.shutdown = true;
        }
    }
}
