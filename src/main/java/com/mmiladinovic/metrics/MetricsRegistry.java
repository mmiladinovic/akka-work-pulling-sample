package com.mmiladinovic.metrics;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;

import java.util.Queue;
import java.util.concurrent.TimeUnit;

/**
 * Created by miroslavmiladinovic on 29/11/2014.
 */
public class MetricsRegistry {
    private static final MetricRegistry registry = new MetricRegistry();
    static {
        JmxReporter reporter = JmxReporter.forRegistry(registry).
                convertDurationsTo(TimeUnit.SECONDS).
                convertRatesTo(TimeUnit.SECONDS).build();
        reporter.start();
    }

    private MetricsRegistry() {
    }

    public static Meter meterWorkGenerated() {
        return registry.meter("work-generated");
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

    public static void registerGaugeMasterQueueDepth(final Queue queue) {
        registry.register(MetricRegistry.name("gauge-master-queue-depth"), new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                return queue.size();
            }
        });
    }
}
