# KafkaTemplate currentSpan tagging issue

## Description

When adding a tag to the current span during the sending of a kafka message using KafkaTemplate,
the tag gets added to another span because the KafkaTemplate doesn't open the scope for the started observation.

The snippet below is the observeSend method in the KafkaTemplate class. It creates an observation object and calls start() but doesn't call openScope().
```java
private CompletableFuture<SendResult<K, V>> observeSend(final ProducerRecord<K, V> producerRecord) {
    Observation observation = ...
    try {
        observation.start();
        // Should be wrapped in try(Scope scope = observation.openScope()) { ... }
        return doSend(producerRecord, observation);
    } catch (RuntimeException ex) {
        ...
    }
```

The micrometer documentation explains how to migrate from sleuth to micrometer.  
The KafkaTemplate.observeSend() method uses the manual approach and according to the explanation, a call to openScope and corresponding close should be added.
See: https://github.com/micrometer-metrics/micrometer/wiki/Migrating-to-new-1.10.0-Observation-API#you-want-to-do-everything-manually-or-you-want-to-signal-events

```java
Observation observation = ...;
observation.start();
try (Scope scope = observation.openScope()) {
  // do some work
  observation.event("look what happened");
  return something;
} catch (Exception exception) {
  ...
}
```

## Reproduction scenario

### Given

- Kafka broker (See Docker compose.yml)
- Zipkin server (See Docker compose.yml)
- Spring Boot 3.2 web application with Micrometer and Zipkin
- Rest controller with injected Kafka template
- Kafka template with injected producer interceptor
- Producer interceptor with injected micrometer tracer

### When

Simplified logical flow

```mermaid
sequenceDiagram
    participant user as User
    participant controller as RestController
    participant template as KafkaTemplate
    participant interceptor as ProducerInterceptor
    participant kafka as KafkaProducer
    participant micrometer as Micrometer
    user ->> controller: http://localhost:8080/api/v1/messages
    controller ->>+ micrometer: start http span
    controller ->> template: send message
    template ->>+ micrometer: start kafka span
    template ->> interceptor: onSend()
    interceptor ->> micrometer: currentSpan.tag(key, value)
    template ->> kafka: send()
    micrometer -->- template: end kafka span
    template -->> controller: message sent
    micrometer -->- controller: end http span
    controller -->> user: response
```

### Then

- The added tags end up in the span of the http request instead of the span of the kafka template

![tag-actual.png](images/tag-actual.png)

### Expected

- The dynamically added tags should end up in the span of the kafka template

![tag-expected.png](images/tag-expected.png)

### Workaround

Extend the KafkaTemplate class, override the `doSend(producerRecord, observation)` method and 
enclose the super method call with opening and closing of the scope.

```java
@Override
protected CompletableFuture<SendResult<K, V>> doSend(
            final ProducerRecord<K, V> producerRecord,
            Observation observation) {
    try (var scope = observation.openScope()) {
        return super.doSend(producerRecord, observation);
    }
}
```

Normally this fix should be done in the `observeSend(producerRecord)` method but unfortunately
that method is `private`

The code for the workaround can be found in the `workaround` branch.
