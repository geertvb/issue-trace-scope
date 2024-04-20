package org.b8.issue.trace;

import io.micrometer.observation.Observation;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

public class FixedKafkaTemplate<K, V> extends KafkaTemplate<K, V> {

    public FixedKafkaTemplate(ProducerFactory<K, V> producerFactory) {
        super(producerFactory);
    }

    @Override
    protected CompletableFuture<SendResult<K, V>> doSend(final ProducerRecord<K, V> producerRecord,
                                                         Observation observation) {
        try (var scope = observation.openScope()) {
            return super.doSend(producerRecord, observation);
        }
    }

}
