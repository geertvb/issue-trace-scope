package org.b8.issue.trace;

import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.core.KafkaTemplate;

@SpringBootApplication
public class IssueTraceScopeApplication {

    public static void main(String[] args) {
        SpringApplication.run(IssueTraceScopeApplication.class, args);
    }

    @Autowired
    public void setKafkaTemplate(
            KafkaTemplate<String, String> kafkaTemplate,
            ProducerInterceptor<String, String> interceptor) {
        kafkaTemplate.setProducerInterceptor(interceptor);
    }

}
