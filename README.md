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

## flowend to end flow
User -> Order Service (SYNC)
Order Service -> Kafka (ORDER_CREATED)
Payment Service -> Kafka (PAYMENT_SUCCESS)
Order Service -> Kafka (PAID)
Delivery Service -> Kafka (OUT_FOR_DELIVERY)
Delivery Service -> Kafka (DELIVERED)
Order Service updates final state
