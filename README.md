+# Food Delivery Platform (Microservices)  
+### End-to-End Backend + Frontend System Built from Scratch
 

+A full-stack, production-style **food delivery platform** designed and implemented from scratch using **Java Spring Boot microservices**, **event-driven architecture (Kafka)**, and a ** JavaScript frontend**.
 

+This project demonstrates how a real-world distributed system can handle:
+- user onboarding and authentication,
+- restaurant discovery and menu browsing,
+- order lifecycle management,
+- asynchronous payment and delivery orchestration,
+- admin operations,
+- observability, tracing, and centralized logging.
+
+---
+
+
+Unlike a monolithic CRUD app, this system is split into independently deployable services behind an API gateway, with async communication for critical business flows.
+
+Key engineering highlights:
+- **Microservices architecture** with clear domain boundaries.
+- **API Gateway + JWT-based authorization**.
+- **Event-driven workflow** using Kafka for order/payment/delivery lifecycle.
+- **Polyglot persistence** (MySQL + MongoDB + Redis).
+- **Operational tooling**: Prometheus, Grafana, ELK, Zipkin.
+- **Role-aware frontend UX** for end users and admins.
+
+---
+
+## Architecture overview
+
+### Core services
+
+| Service | Port | Responsibility |
+|---|---:|---|
+| Service Discovery (Eureka) | 8761 | Service registry and discovery |
+| API Gateway | 8080 | Routing, JWT validation, centralized entry point |
+| User Service | 8081 | Registration, login, password flows, user management |
+| Restaurant Service | 8082 | Restaurants, menus, availability, search |
+| Order Service | 8083 | Order creation, status transitions, complaint workflows |
+| Payment Service | 8084 | Payment processing (async) |
+| Delivery Service | 8085 | Delivery assignment and delivery status events |
+
+### Supporting infrastructure
+- **Kafka + Zookeeper** for asynchronous eventing.
+- **MySQL** for transactional/order data.
+- **MongoDB** for document-oriented service data.
+- **Redis** for caching/supporting state.
+- **Prometheus + Grafana** for metrics and dashboards.
+- **ELK stack (Elasticsearch, Logstash, Kibana)** for centralized logs.
+- **Zipkin** for distributed tracing.
+
+---
+
+## End-to-end business flow
+
+### 1) Authentication and authorization
+1. User signs up / logs in through API Gateway.
+2. User service issues JWT token containing identity and role.
+3. Client includes JWT in protected requests.
+4. Gateway/service layer enforces role-based access.
+
+### 2) Order lifecycle (event-driven)
+1. User places order.
+2. Order service persists order and publishes `ORDER_CREATED`.
+3. Payment service consumes event and emits `PAYMENT_SUCCESS` / `PAYMENT_FAILED`.
+4. Order service updates payment state and emits confirmed order event.
+5. Delivery service consumes event, assigns rider, and emits delivery progress events.
+6. Order service updates final order state (e.g., `DELIVERED`).
+
+This decoupled flow enables resilience and scalability while keeping domain services independently evolvable.
+
+---
+
+## Tech stack
+
+### Backend
 - Java 17
 - Spring Boot
 - Spring Cloud Gateway
+- Spring Cloud Netflix Eureka
+- Spring Data JPA / Spring Data MongoDB
+- Spring Security (JWT-based)
+- Kafka
+
+### Datastores & infra
 - MySQL
+- MongoDB
+- Redis
+- Docker / Docker Compose
+
+### Observability
+- Prometheus
+- Grafana
+- ELK Stack
+- Zipkin
+
+### Frontend
+- HTML, CSS, JavaScript
+
+---
+
+## Repository structure
+
+```text
+food-delivery-fullstack/
+├── api-gateway/
+├── service-discovery/
+├── user/
+├── restaurant/
+├── order/
+├── payment/
+├── delivery/
+├── food-delivery-events/      # shared event contracts
+├── frontend/                  # user/admin web UI
+├── docker-compose.yml
+├── prometheus.yml
+└── docs/
+```
+
+---
+
+## Getting started
+
+## 1) Prerequisites
+- Java 17+
+- Maven 3.8+
+- Docker + Docker Compose
+
+## 2) Start infrastructure
+
+```bash
+docker compose up -d
+```
+
+This brings up Kafka, Zookeeper, MySQL, MongoDB, Redis, Prometheus, Grafana, ELK, and Zipkin.
+
+## 3) Create required MySQL databases
+
+```bash
+docker exec -it mysql mysql -uroot -proot
+CREATE DATABASE userdb;
+CREATE DATABASE orderdb;
+SHOW DATABASES;
+```
+
+## 4) Start microservices (recommended order)
+1. `service-discovery`
+2. `api-gateway`
+3. `user`
+4. `restaurant`
+5. `order`
+6. `payment`
+7. `delivery`
+
+You can run each service with:
+
+```bash
+./mvnw spring-boot:run
+```
+
+(from each service directory)
+
+## 5) Run frontend
+Open `frontend/index.html` in browser.

+
+## Observability endpoints
+
+- Eureka: `http://localhost:8761`
+- API Gateway: `http://localhost:8080`
+- Prometheus: `http://localhost:9090`
+- Grafana: `http://localhost:3001`
+- Kibana: `http://localhost:5601`
+- Zipkin: `http://localhost:9411`
+
+---
