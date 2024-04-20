package org.b8.issue.trace;

import io.micrometer.tracing.Tracer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class TaggingProducerInterceptor implements ProducerInterceptor<String, String> {

    @Autowired
    Tracer tracer;

    @Override
    public ProducerRecord<String, String> onSend(ProducerRecord<String, String> producerRecord) {
        log.trace("Sending record: {}", producerRecord);
        tracer.currentSpanCustomizer().tag("key", "value");
        return producerRecord;
    }

    @Override
    public void onAcknowledgement(RecordMetadata recordMetadata, Exception e) {
        log.trace("Acknowledging record: {}", recordMetadata);
    }

    @Override
    public void close() {
        log.trace("Closing producer interceptor");
    }

    @Override
    public void configure(Map<String, ?> map) {
        log.trace("Configuring producer interceptor");
    }

}
