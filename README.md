# Food Delivery Platform

Microservices-based food delivery platform built using Java and Spring Boot.

## Tech Stack
- Java 17
- Spring Boot
- Spring Cloud Gateway
- Eureka Service Registry
- JWT Authentication
- MongoDB
- MySQL
- Kafka 

## Services
- API Gateway
- User Service
- Restaurant Service
- Order Service 
- payment service
- delivery service

## How to Run
1. Start Eureka Server
2. Start API Gateway
3. Start individual services

## end to end flow
1) Client -> Order Service (SYNC)
2) Order Service -> order-events (CREATED)
3) Payment Service -> payment-events (SUCCESS)
4) Order Service -> status = PAID
5) Delivery Service -> delivery-events (OUT_FOR_DELIVERY)
6) Order Service -> status = OUT_FOR_DELIVERY
7) Delivery Service -> delivery-events (DELIVERED)
8) Order Service -> status = DELIVERED
