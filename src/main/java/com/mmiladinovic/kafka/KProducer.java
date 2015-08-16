package com.mmiladinovic.kafka;

import com.google.gson.Gson;
import com.mmiladinovic.model.AdImpression;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

/**
 * Created by miroslavmiladinovic on 16/08/15.
 */
public class KProducer {

    private final String kBrokers;
    private final String topic;
    private final KafkaProducer<String, String> producer;

    private final Gson gson = new Gson();

    public KProducer(String kBrokers, String topic) {
        this.kBrokers = kBrokers;
        this.topic = topic;

        Properties props = new Properties();
        props.put("bootstrap.servers", kBrokers);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        producer = new KafkaProducer<>(props);
    }

    public void send(AdImpression imp) {
        if (imp != null) {
            final String key = String.valueOf(imp.getAppId());
            final String value = gson.toJson(imp);
            producer.send(new ProducerRecord<>(topic, key, value));
        }
    }
}
