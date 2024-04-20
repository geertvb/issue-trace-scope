# Trace scope issue

## build

Build the test application
```shell
mvn clean install
```

## Infrastructure

Run Kafka and zipkin

```shell
docker compose up -d
```

## Run

```shell
mvn spring-boot:run
```

## Test

```http request
POST http://localhost:8080/api/v1/messages
```

## Check trace

```http request
http://localhost:9411
```