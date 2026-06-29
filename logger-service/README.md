# Logger Service — Alert Hub

Centralized logging microservice. It **consumes log messages from a Kafka topic** (`logs`) and **stores them in MongoDB**, so every other microservice (Processor, Email, SMS, etc.) can ship its logs to one place. Read endpoints are exposed for querying stored logs.

## Architecture

```
Other services ──(JSON LogMessage)──▶ Kafka topic "logs" ──▶ LogConsumer ──▶ LogService ──▶ MongoDB ("logs" collection)
                                                                                                   │
                                                              GET /api/logs ◀── LogController ◀─────┘
```

## Tech Stack

- Java 17, Spring Boot 3.5.x
- Spring for Apache Kafka (consumer)
- Spring Data MongoDB
- Lombok
- Docker (MongoDB + Kafka)

## Prerequisites

- JDK 17+
- Docker Desktop
- Postman (for testing)

---

## 1. Start the infrastructure (Docker)

### MongoDB

```bash
docker run -d --name alerthub-mongo -p 27017:27017 mongo:7
```

### Kafka (official Apache image, KRaft mode — no Zookeeper)

```bash
docker pull apache/kafka:latest

docker run -d --name alerthub-kafka -p 9092:9092 -e KAFKA_NODE_ID=0 -e KAFKA_PROCESS_ROLES=broker,controller -e KAFKA_LISTENERS=PLAINTEXT://:9092,CONTROLLER://:9093 -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 -e KAFKA_CONTROLLER_QUORUM_VOTERS=0@localhost:9093 -e KAFKA_CONTROLLER_LISTENER_NAMES=CONTROLLER -e KAFKA_LISTENER_SECURITY_PROTOCOL_MAP=CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 -e KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS=0 apache/kafka:latest
```

### Verify both containers are running

```bash
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
```

Expected:

| Name | Status | Ports |
|------|--------|-------|
| alerthub-mongo | Up | 0.0.0.0:27017->27017/tcp |
| alerthub-kafka | Up | 0.0.0.0:9092->9092/tcp |

---

## 2. Run the application

```bash
cd logger-service
./mvnw spring-boot:run          # macOS/Linux
.\mvnw.cmd spring-boot:run      # Windows
```

The service starts on **http://localhost:8086**.

## Configuration (`src/main/resources/application.properties`)

| Property | Value |
|----------|-------|
| `server.port` | `8086` |
| `spring.data.mongodb.uri` | `mongodb://localhost:27017/logger-service` |
| `spring.kafka.bootstrap-servers` | `localhost:9092` |
| `spring.kafka.consumer.group-id` | `logger-service` |
| Kafka topic | `logs` |

---

## 3. Test with Postman

### Get all logs

```
GET http://localhost:8086/api/logs
```

Returns `200 OK` with `[]` when empty.

### Filter by service

```
GET http://localhost:8086/api/logs?serviceName=processor
```

### Filter by level

```
GET http://localhost:8086/api/logs?level=INFO
```

---

## 4. Test the full Kafka → MongoDB flow

Since logs arrive via Kafka, push a test message into the `logs` topic.

### Open the Kafka console producer

```bash
docker exec -it alerthub-kafka /opt/kafka/bin/kafka-console-producer.sh --bootstrap-server localhost:9092 --topic logs
```

At the `>` prompt, paste a message and press Enter:

```json
{"serviceName":"processor","logLevel":"INFO","message":"first test log","timestamp":"2026-06-18T11:00:00Z"}
```

Press `Ctrl+C` to exit.

### Verify

Call `GET http://localhost:8086/api/logs` in Postman — the message should now appear:

```json
[
  {
    "id": "66718f...",
    "timestamp": "2026-06-18T11:00:00Z",
    "serviceName": "processor",
    "logLevel": "INFO",
    "message": "first test log"
  }
]
```

### Check directly in MongoDB (optional)

```bash
docker exec -it alerthub-mongo mongosh logger-service --eval "db.logs.find().pretty()"
```

---

## Log message format

Producers send this JSON to the `logs` topic:

| Field | Type | Notes |
|-------|------|-------|
| `serviceName` | String | name of the source microservice |
| `logLevel` | String | INFO / DEBUG / WARN / ERROR |
| `message` | String | the log text |
| `timestamp` | ISO-8601 String | optional; the logger sets "now" if missing |

---

## Troubleshooting

| Symptom | Cause / Fix |
|---------|-------------|
| App starts but logs never save | Check `spring.json.value.default.type` = `com.alerthub.logger_service.dto.LogMessage` |
| `Connection to node -1 could not be established` | Kafka container not running — `docker ps`, restart `alerthub-kafka` |
| Mongo connection refused | `alerthub-mongo` not running on port 27017 |
| Kafka container keeps restarting | `docker logs alerthub-kafka` to inspect |

## Stop everything

```bash
docker stop alerthub-kafka alerthub-mongo
```
