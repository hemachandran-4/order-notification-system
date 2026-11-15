<div align="center">

# 📦 Order Notification System

### Event-driven microservices with Spring Boot, Apache Kafka, MySQL & Docker

![Java](https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F?logo=springboot&logoColor=white)
![Kafka](https://img.shields.io/badge/Apache%20Kafka-KRaft-231F20?logo=apachekafka&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.4-4479A1?logo=mysql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white)
[![CI](https://github.com/YOUR_USERNAME/order-notification-system/actions/workflows/ci.yml/badge.svg)](https://github.com/YOUR_USERNAME/order-notification-system/actions)

*Place an order → an event flies through Kafka → a notification is delivered.*
*Two independent services. Zero direct coupling.*

</div>

---

## ✨ Features

- 🛒 **Order Service**: REST API to create and fetch orders, persisted in MySQL
- 📨 **Event publishing**: emits an `OrderCreatedEvent` to Kafka on every new order
- 🔔 **Notification Service**: consumes events, "sends" the notification, stores the result
- 🔁 **Resilience**: automatic retries with backoff and a Dead Letter Topic
- 🧱 **Idempotent consumer**: duplicate events never create duplicate notifications
- 🗄️ **Database per service**, versioned with Flyway migrations
- 🐳 **One-command startup** with Docker Compose
- ✅ **Integration tests** with Testcontainers and **CI** via GitHub Actions

## 🏗️ Architecture

```mermaid
flowchart LR
    C([Client]) -->|POST /orders| O[order-service :8081]
    O -->|save| DB1[(orders_db)]
    O -->|OrderCreatedEvent| K{{Kafka<br/>order-created}}
    K --> N[notification-service :8082]
    N -->|save| DB2[(notifications_db)]
    K -. failed after retries .-> D{{order-created.DLT}}
```

## 🧰 Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 17 |
| Framework | Spring Boot 3 (Web, Data JPA, Validation, Actuator) |
| Messaging | Apache Kafka (KRaft mode, no Zookeeper) |
| Database | MySQL 8.4 + Flyway |
| Containers | Docker, Docker Compose |
| Testing | JUnit 5, Testcontainers |
| CI | GitHub Actions |

## 🚀 Quick Start

**Prerequisites:** Docker and Docker Compose. That's it.

```bash
git clone https://github.com/hemachandran-4/order-notification-system.git
cd order-notification-system
docker compose up --build
```

Wait until both services report healthy, then try it out 👇

## 🧪 Try It

**1. Create an order**
```bash
curl -X POST http://localhost:8081/orders \
  -H "Content-Type: application/json" \
  -d '{"customerEmail":"jane@example.com","total":149.99}'
```

**2. Fetch the order**
```bash
curl http://localhost:8081/orders/1
```

**3. See the notification that was created asynchronously**
```bash
curl http://localhost:8082/notifications
```

## 📡 API Reference

| Service | Method | Endpoint | Description |
|---------|--------|----------|-------------|
| order-service | `POST` | `/orders` | Create an order and publish an event |
| order-service | `GET` | `/orders/{id}` | Get an order by ID |
| notification-service | `GET` | `/notifications` | List all notifications |
| both | `GET` | `/actuator/health` | Health check |

## 📁 Project Structure

```
order-notification-system/
├── docker-compose.yml
├── init.sql
├── order-service/
│   ├── Dockerfile
│   └── src/main/java/.../{controller,service,repository,model,event}
└── notification-service/
    ├── Dockerfile
    └── src/main/java/.../{listener,service,repository,model}
```

## 🧠 Design Decisions

- **Database per service**: services share nothing but events, so each can evolve and scale independently.
- **Kafka KRaft mode**: fewer moving parts than Zookeeper, and the modern default.
- **Order ID as message key**: events for the same order always land in the same partition, which preserves ordering.
- **Dead Letter Topic**: poison messages never block the consumer, and failures stay inspectable.
- **Idempotent consumer**: Kafka guarantees at-least-once delivery, so the consumer de-duplicates by order ID.

## ✅ Running Tests

```bash
mvn verify        # spins up real MySQL and Kafka via Testcontainers
```

## 🗺️ Roadmap

- [x] Order and notification services
- [x] Retries and Dead Letter Topic
- [x] Idempotent consumer
- [x] Testcontainers and CI
- [ ] Transactional outbox pattern
- [ ] OpenAPI / Swagger UI
- [ ] Real email delivery (SMTP / SendGrid)

## 👤 Author

**Hemachandran** · [LinkedIn](https://www.linkedin.com/in/hemachandran-j4848/) · [GitHub](https://github.com/hemachandran-4)

