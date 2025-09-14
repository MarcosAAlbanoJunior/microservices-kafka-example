# 🚀 Microserviços com Apache Kafka - Guia de Estudos

Este repositório demonstra a implementação de microserviços utilizando Apache Kafka como sistema de mensageria, com padrões de Producer e Consumer para comunicação assíncrona entre serviços.

## 📋 Índice
- [Visão Geral](#-visão-geral)
- [Arquitetura](#-arquitetura)
- [Conceitos Kafka](#-conceitos-kafka)
- [Configurações do Projeto](#-configurações-do-projeto)
- [Como Executar](#-como-executar)
- [Testando a Aplicação](#-testando-a-aplicação)
- [Monitoramento](#-monitoramento)

## 🎯 Visão Geral

O projeto implementa dois microserviços:
- **Products Microservice** (Producer): Publica eventos de criação de produtos
- **Email Notification Microservice** (Consumer): Consome eventos e processa notificações por email

### Tecnologias Utilizadas
- **Apache Kafka** - Sistema de streaming de eventos
- **Spring Boot** - Framework para microserviços
- **Spring Kafka** - Integração Spring com Kafka
- **Docker & Docker Compose** - Containerização

## 🏗️ Arquitetura

```
┌─────────────────┐    produce    ┌─────────────────┐    consume    ┌─────────────────────────┐
│                 │  ──────────►  │                 │  ──────────► │                         │
│ Products        │               │ Kafka Cluster   │              │ Email Notification      │
│ Microservice    │               │ (3 Brokers)     │              │ Microservice            │
│ (Producer)      │               │                 │              │ (Consumer)              │
└─────────────────┘               └─────────────────┘              └─────────────────────────┘
```

## 📚 Conceitos Kafka

### 🎪 **Brokers**
Os brokers são os servidores Kafka que formam o cluster. No projeto temos **3 brokers**:
- `kafka-1:9090` (porta externa: 9092)
- `kafka-2:9090` (porta externa: 9094) 
- `kafka-3:9090` (porta externa: 9096)

**Por que 3 brokers?**
- ✅ **Alta Disponibilidade**: Se um broker falhar, os outros continuam funcionando
- ✅ **Distribuição de Carga**: As partições são distribuídas entre os brokers
- ✅ **Tolerância a Falhas**: Permite replicação com fator 3

### 📂 **Topics**
Topics são categorias onde as mensagens são armazenadas. No projeto:
- **Topic**: `product-created-events-topic`
- **Partições**: 3 (distribuídas pelos 3 brokers)
- **Réplicas**: 3 (cada partição tem 3 cópias)

```java
@Bean
public NewTopic createTopic() {
    return TopicBuilder.name(PRODUCT_CREATED_EVENTS_TOPIC)
            .partitions(3)        // 3 partições para paralelismo
            .replicas(3)          // 3 réplicas para redundância
            .configs(Map.of("min.insync.replicas", "2"))  // Mínimo 2 réplicas sincronizadas
            .build();
}
```

### 🔀 **Partitions (Partições)**
Partições permitem paralelismo e escalabilidade:
- **3 partições** = até 3 consumers simultâneos
- Mensagens com a mesma **chave** (productId) vão para a mesma partição
- Garante **ordem** de mensagens por chave

### 📤 **Producer (Products Microservice)**
O producer publica mensagens no Kafka. Implementamos **duas estratégias**:

#### **Síncrono** (`/products/sync`)
```java
SendResult<String, ProductCreatedEvent> result = kafkaTemplate
    .send(TOPIC, productId, event)
    .get(30, TimeUnit.SECONDS);  // Espera confirmação por até 30s
```
- ✅ **Garantia de entrega**: Aguarda confirmação
- ❌ **Menor throughput**: Bloqueia até confirmação
- 🎯 **Use quando**: Dados críticos que precisam ser confirmados

#### **Assíncrono** (`/products/async`)
```java
CompletableFuture<SendResult<String, ProductCreatedEvent>> future =
    kafkaTemplate.send(TOPIC, productId, event);

future.whenComplete((result, exception) -> {
    // Processa resultado de forma assíncrona
});
```
- ✅ **Alto throughput**: Não bloqueia
- ❌ **Menos garantias**: Possível perda em casos extremos
- 🎯 **Use quando**: Performance é mais importante que garantia absoluta

### 📥 **Consumer (Email Notification Microservice)**
O consumer processa as mensagens do topic. Principais configurações:

```java

@KafkaListener(topics = "product-created-events-topic")
public void consumeProductCreatedEvent(ProductCreatedEvent event) {
    // Processa evento
}
```

### ⚠️ **DLT (Dead Letter Topic)**
**Conceito**: Quando uma mensagem falha no processamento após várias tentativas, ela é enviada para um "topic de mensagens mortas" para análise posterior.

**Implementação típica**:
```java
@RetryableTopic(
        attempts = "5",
        backoff = @Backoff(delay = 1000, multiplier = 2.0, maxDelay = 30000),
        dltTopicSuffix = ".DLT"
)
@KafkaListener(topics="product-created-events-topic", groupId = "product-created-events")
public class ProductCreatedEventHandler {
}
```

## ⚙️ Configurações do Projeto

### 🔧 **Configurações de Reliability**

```java
// Garante que todas as réplicas confirmem a escrita
spring.kafka.producer.acks=all

// Evita duplicação de mensagens
spring.kafka.producer.properties.enable.idempotence=true

// Controla quantas requisições podem estar "em voo" simultaneamente
spring.kafka.producer.properties.max.in.flight.requests.per.connection=5
```

### ⏱️ **Configurações de Timeout**

```java
// Tempo total para entrega da mensagem (2 minutos)
spring.kafka.producer.properties.delivery.timeout.ms=120000

// Tempo para aguardar resposta do broker (30 segundos)
spring.kafka.producer.properties.request.timeout.ms=30000

// Tempo para aguardar batch de mensagens (0 = envio imediato)
spring.kafka.producer.properties.linger.ms=0
```

### 📝 **Configurações de Serialização**

```java
// Chave como String
spring.kafka.producer.key-serializer=StringSerializer

// Valor como JSON
spring.kafka.producer.value-serializer=JsonSerializer

// Remove headers de tipo (mais limpo)
spring.kafka.producer.properties.spring.json.add.type.headers=false
```

### 🛡️ **Configurações de Consistência**

```java
// No código Java - mínimo de réplicas que devem confirmar
.configs(Map.of("min.insync.replicas", "2"))

// 3 réplicas com mínimo 2 sincronizadas = tolerância a 1 falha
```

## 🚀 Como Executar

### 1. **Pré-requisitos**
- Docker e Docker Compose
- Java 17+
- Maven

### 2. **Iniciando o Ambiente**
```bash
# Clona o repositório
git clone https://github.com/MarcosAAlbanoJunior/microservices-kafka-example
cd microservices-kafka-example

# Inicia Kafka + Microserviços
docker-compose -f docker-compose.yml --env-file environment.env up
obs: O processo pode levar alguns minutos, já que os microserviços dependem do Kafka estar pronto. Durante esse tempo, você verá várias mensagens de inicialização nos logs. Aguarde até que todos os serviços estejam marcados como healthy antes de começar a utilizá-los. Vai aparecer erros de org.apache.kafka.clients.NetworkClient até estabilizarem.

# Inicia apenas o Kafka
Caso preferir inciar só o kafka via docker e iniciar os microsserviços pela sua IDE use este compose
docker-compose -f docker-compose-kafka.yml --env-file environment.env up
```

### 3. **Verificando o Cluster**
```bash
# Lista topics
docker exec kafka-1 kafka-topics.sh --bootstrap-server localhost:9090 --list

# Descreve o topic
docker exec kafka-1 kafka-topics.sh --bootstrap-server localhost:9090 --describe --topic product-created-events-topic
```

## 🧪 Testando a Aplicação

### **Criar Produto (Síncrono)**
```bash
curl -X POST http://localhost:8081/products/sync \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Notebook Gamer",
    "price": 2500.00,
    "quantity": 10
  }'
```

### **Criar Produto (Assíncrono)**
```bash
curl -X POST http://localhost:8081/products/async \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Mouse Gamer",
    "price": 150.00,
    "quantity": 50
  }'
```

### **Monitorar Mensagens**
```bash
# Consome mensagens do topic
docker exec kafka-1 kafka-console-consumer.sh \
  --bootstrap-server localhost:9090 \
  --topic product-created-events-topic \
  --from-beginning \
  --property print.key=true
```

## 📊 Monitoramento

### **Logs dos Microserviços**
```bash
# Products Microservice
docker logs products-microservice -f

# Email Notification Microservice  
docker logs email-notification-microservice -f
```

### **Métricas do Cluster**
```bash
# Status dos brokers
docker exec kafka-1 kafka-broker-api-versions.sh \
  --bootstrap-server kafka-1:9090,kafka-2:9090,kafka-3:9090

# Consumer groups
docker exec kafka-1 kafka-consumer-groups.sh \
  --bootstrap-server localhost:9090 --list
```

## 🎓 Aprendizados

### **Quando usar Síncrono vs Assíncrono?**

| Cenário | Síncrono | Assíncrono |
|---------|----------|------------|
| **Pagamentos** | ✅ | ❌ |
| **Auditoria** | ✅ | ❌ |
| **Notificações** | ❌ | ✅ |
| **Logs/Métricas** | ❌ | ✅ |
| **Alto Volume** | ❌ | ✅ |

### **Configurações por Cenário**

| Cenário | acks | idempotence | min.insync.replicas |
|---------|------|-------------|-------------------|
| **Máxima Confiabilidade** | all | true | 2+ |
| **Alto Throughput** | 1 | false | 1 |
| **Balanceado** | all | true | 2 |

## 🔗 Recursos Úteis

- [Kafka Documentation](https://kafka.apache.org/documentation/)
- [Spring Kafka Reference](https://spring.io/projects/spring-kafka)
- [Kafka Best Practices](https://kafka.apache.org/documentation/#bestpractices)

---

**💡 Dica**: Este projeto é ideal para entender conceitos fundamentais do Kafka. Experimente modificar as configurações e observar o comportamento!