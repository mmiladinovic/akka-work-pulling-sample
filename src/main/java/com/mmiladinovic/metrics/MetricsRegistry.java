package com.mmiladinovic.metrics;

import com.codahale.metrics.*;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.concurrent.TimeUnit;

/**
 * Created by miroslavmiladinovic on 29/11/2014.
 */
public class MetricsRegistry {
    private static final MetricRegistry registry = new MetricRegistry();

    static {
//        JmxReporter reporter = JmxReporter.forRegistry(registry).
//                convertDurationsTo(TimeUnit.SECONDS).
//                convertRatesTo(TimeUnit.SECONDS).build();
//        reporter.start();

        final Slf4jReporter reporter = Slf4jReporter.forRegistry(registry)
                .outputTo(LoggerFactory.getLogger("com.mmiladinovic.metrics"))
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();
        reporter.start(10, TimeUnit.SECONDS);

    }

    private MetricsRegistry() {
    }

    public static Meter meterWorkDequeued() {
        return registry.meter("work-dequeued");
    }

    public static Timer timerKafkaRead() {
        return registry.timer("timer-kafka-consumer");
    }

    public static Meter meterWorkAccepted() {
        return registry.meter("work-accepted");
    }

    public static Meter meterWorkRejected() {
        return registry.meter("work-rejected");
    }

    public static Meter meterWorkCompleted() {
        return registry.meter("work-completed");
    }

    public static Meter meterProdForWork() {
        return registry.meter("work-prodded");
    }

    public static Meter meterAskForWorkInIdle() {
        return registry.meter("work-ask-in-idle");
    }

    public static Histogram histogramProcessingLatency() {
        return registry.histogram("work-processing-latency");
    }

    public static void registerGaugeMasterQueueDepth(final Queue queue) {
        registry.register(MetricRegistry.name("gauge-master-queue-depth"), (Gauge<Integer>) queue::size);
    }

    public static void registerKafkaConsumerQueueDepth(final Queue queue) {
        registry.register(MetricRegistry.name("kafka-consumer-queue-depth"), (Gauge<Integer>) queue::size);
    }


    // -- TestDriver

    public static Meter meterWorkEnqueued() {
        return registry.meter("work-enqueued");
    }
}
