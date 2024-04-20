package org.b8.issue.trace;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/messages")
public class MessageController {

    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    @PostMapping
    public void postMessage() {
        kafkaTemplate.send("topic", "key", "value");
    }

}
