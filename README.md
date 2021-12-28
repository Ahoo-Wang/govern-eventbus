# Govern EventBus

> [中文文档](./README.zh-CN.md)

*Govern EventBus* is an *event-driven architecture* framework that has been validated in a four-year production
environment, which governs remote procedure calls between microservices through event bus mechanism. Strong consistency
within microservices is supported by local transactions, and final consistency between microservices is achieved by
event bus. In addition, automatic compensation of event publish / subscribe is provided.

## Execution Flow

<p align="center"><img src="./docs/Govern-EventBus.png" alt="Govern EventBus"/></p>

## Installation

### init db

``` sql

create table simba_mutex
(
    mutex         varchar(66)     not null primary key comment 'mutex name',
    acquired_at   bigint unsigned not null,
    ttl_at        bigint unsigned not null,
    transition_at bigint unsigned not null,
    owner_id      char(32)        not null,
    version       int unsigned    not null
);

create table if not exists cosid_machine
(
    name            varchar(100) not null comment '{namespace}.{machine_id}',
    namespace       varchar(100) not null,
    machine_id      integer      not null default 0,
    last_timestamp  bigint       not null default 0,
    instance_id     varchar(100) not null default '',
    distribute_time bigint       not null default 0,
    revert_time     bigint       not null default 0,
    constraint cosid_machine_pk
        primary key (name)
) engine = InnoDB;

create index if not exists idx_namespace on cosid_machine (namespace);
create index if not exists idx_instance_id on cosid_machine (instance_id);

create table publish_event
(
    id             bigint unsigned auto_increment
        primary key,
    event_name     varchar(100)              not null,
    event_data_id  bigint unsigned default 0 not null,
    event_data     mediumtext                not null,
    status         smallint unsigned         not null,
    published_time bigint unsigned default 0 not null,
    version        smallint unsigned         not null,
    create_time    bigint unsigned           not null
);

create
    index idx_status
    on publish_event (status);

create table publish_event_compensate
(
    id               bigint unsigned auto_increment
        primary key,
    publish_event_id bigint unsigned not null,
    start_time       bigint unsigned not null,
    taken            bigint unsigned not null,
    failed_msg       text            null
);

create table publish_event_failed
(
    id               bigint unsigned auto_increment
        primary key,
    publish_event_id bigint unsigned not null,
    failed_msg       text            not null,
    create_time      bigint unsigned not null
);

create table subscribe_event
(
    id                bigint unsigned auto_increment
        primary key,
    subscribe_name    varchar(100)              not null,
    status            smallint unsigned         not null,
    subscribe_time    bigint unsigned           not null,
    event_id          bigint unsigned           not null,
    event_name        varchar(100)              not null,
    event_data_id     bigint unsigned default 0 not null,
    event_data        mediumtext                not null,
    event_create_time bigint unsigned           not null,
    version           smallint unsigned         not null,
    create_time       bigint unsigned           not null,
    constraint uk_subscribe_name_even_id_event_name
        unique (subscribe_name, event_id, event_name)
);

create
    index idx_status
    on subscribe_event (status);

create table subscribe_event_compensate
(
    id                 bigint unsigned auto_increment
        primary key,
    subscribe_event_id bigint unsigned not null,
    start_time         bigint unsigned not null,
    taken              int unsigned    not null,
    failed_msg         text            null
);

create table subscribe_event_failed
(
    id                 bigint unsigned auto_increment
        primary key,
    subscribe_event_id bigint unsigned not null,
    failed_msg         text            not null,
    create_time        bigint unsigned not null
);

insert into simba_mutex
    (mutex, acquired_at, ttl_at, transition_at, owner_id, version)
values ('eventbus_publish_leader', 0, 0, 0, '', 0);

insert into simba_mutex
    (mutex, acquired_at, ttl_at, transition_at, owner_id, version)
values ('eventbus_subscribe_leader', 0, 0, 0, '', 0);

```

### Gradle

> Kotlin DSL

```kotlin
    val eventbusVersion = "1.0.5";
    implementation("me.ahoo.eventbus:eventbus-spring-boot-starter:${eventbusVersion}")
    implementation("me.ahoo.eventbus:eventbus-spring-boot-autoconfigure:${eventbusVersion}") {
        capabilities {
            requireCapability("me.ahoo.eventbus:rabbit-bus-support")
            //requireCapability("me.ahoo.eventbus:kafka-bus-support")
        }
    }
```

### Maven

```xml
<?xml version="1.0" encoding="UTF-8"?>

<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>
    <artifactId>demo</artifactId>
    <properties>
        <eventbus.version>1.0.5</eventbus.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>me.ahoo.eventbus</groupId>
            <artifactId>eventbus-spring-boot-starter</artifactId>
            <version>${eventbus.version}</version>
        </dependency>
        <dependency>
            <groupId>me.ahoo.eventbus</groupId>
            <artifactId>eventbus-rabbit</artifactId>
            <version>${eventbus.version}</version>
        </dependency>
        <!--<dependency>-->
        <!--    <groupId>me.ahoo.eventbus</groupId>-->
        <!--    <artifactId>eventbus-kafka</artifactId>-->
        <!--    <version>${eventbus.version}</version>-->
        <!--</dependency>-->
    </dependencies>
</project>
```

### Spring Boot Application Config

```yaml
spring:
  application:
    name: eventbus-demo
  rabbitmq:
    host: localhost
    username: eventbus
    password: eventbus

  shardingsphere:
    datasource:
      names: ds0
      ds0:
        type: com.zaxxer.hikari.HikariDataSource
        jdbcUrl: jdbc:mariadb://localhost:3306/eventbus_db?serverTimezone=GMT%2B8&characterEncoding=utf-8
        username: root
        password: root
    props:
      sql-show: true
    rules:
      sharding:
        tables:
          publish_event:
            actual-data-nodes: ds0.publish_event_$->{202110..202112},ds0.publish_event_$->{202201..202212}
            table-strategy:
              standard:
                sharding-column: create_time
                sharding-algorithm-name: publish-event-interval
          subscribe_event:
            actual-data-nodes: ds0.subscribe_event_$->{202110..202112},ds0.subscribe_event_$->{202201..202212}
            table-strategy:
              standard:
                sharding-column: event_create_time
                sharding-algorithm-name: subscribe-event-interval
        sharding-algorithms:
          publish-event-interval:
            type: COSID_INTERVAL
            props:
              logic-name-prefix: publish_event_
              datetime-lower: 2021-10-01 00:00:00
              datetime-upper: 2022-12-31 23:59:59
              sharding-suffix-pattern: yyyyMM
              datetime-interval-unit: MONTHS
              datetime-interval-amount: 1
          subscribe-event-interval:
            type: COSID_INTERVAL
            props:
              logic-name-prefix: subscribe_event_
              datetime-lower: 2021-10-01 00:00:00
              datetime-upper: 2022-12-31 23:59:59
              sharding-suffix-pattern: yyyyMM
              datetime-interval-unit: MONTHS
              datetime-interval-amount: 1
govern:
  eventbus:
    rabbit:
      exchange: eventbus
    compensate:
      publish:
        schedule:
          initial-delay: 30s
          period: 10s
        range: 60D
      subscribe:
        schedule:
          initial-delay: 30s
          period: 10s
      enabled: true
    subscriber:
      prefix: ${spring.application.name}.

cosid:
  namespace: ${spring.application.name}
  segment:
    enabled: true
    mode: chain
    chain:
      safe-distance: 1
    distributor:
      jdbc:
        enable-auto-init-id-segment: true
    provider:
      eventbus:
        step: 100
```

## Get Started

> Generally, the *publisher* and the *subscriber* are not in the same application service.
> This is just for demonstration purposes.。

> [Demo](./eventbus-demo)

### Publisher

```java
/**
 * Define publishing events
 */
public class OrderCreatedEvent {
    private long orderId;

    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }

    @Override
    public String toString() {
        return "OrderCreatedEvent{" +
                "orderId=" + orderId +
                "}";
    }
}
```

```java
package me.ahoo.eventbus.demo.service;

import me.ahoo.eventbus.core.annotation.Publish;
import me.ahoo.eventbus.demo.event.OrderCreatedEvent;
import org.springframework.stereotype.Service;

/**
 * @author ahoo wang
 */
@Service
public class OrderService {

    @Publish
    public OrderCreatedEvent createOrder() {
        OrderCreatedEvent orderCreatedEvent = new OrderCreatedEvent();
        orderCreatedEvent.setOrderId(1L);
        return orderCreatedEvent;
    }
}

```

### Subscriber

```java
package me.ahoo.eventbus.demo.service;

import lombok.extern.slf4j.Slf4j;
import me.ahoo.eventbus.core.annotation.Subscribe;
import me.ahoo.eventbus.demo.event.OrderCreatedEvent;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NoticeService {

    @Subscribe
    public void handleOrderCreated(OrderCreatedEvent orderCreatedEvent) {
        log.info("handleOrderCreated - event:[{}].", orderCreatedEvent);
        /**
         * Execute business code
         * send sms / email ?
         */
    }
}
```
