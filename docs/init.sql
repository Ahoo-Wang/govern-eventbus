create table compensate_leader
(
    name              varchar(16)  not null
        primary key,
    term_start        bigint unsigned not null,
    term_end          bigint unsigned not null,
    transition_period bigint unsigned not null,
    leader_id         varchar(100) not null,
    version           int unsigned not null
);

create table publish_event
(
    id             bigint unsigned auto_increment
        primary key,
    event_name     varchar(100) not null,
    event_data     mediumtext   not null,
    status         smallint unsigned not null,
    published_time bigint unsigned default 0 not null,
    version        smallint unsigned not null,
    create_time    bigint unsigned not null
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
    failed_msg       text null
);

create table publish_event_failed
(
    id               bigint unsigned auto_increment
        primary key,
    publish_event_id bigint unsigned not null,
    failed_msg       text not null,
    create_time      bigint unsigned not null
);

create table subscribe_event
(
    id                bigint unsigned auto_increment
        primary key,
    subscribe_name    varchar(100) not null,
    status            smallint unsigned not null,
    subscribe_time    bigint unsigned not null,
    event_id          bigint unsigned not null,
    event_name        varchar(100) not null,
    event_data        mediumtext   not null,
    event_create_time bigint unsigned not null,
    version           smallint unsigned not null,
    create_time       bigint unsigned not null,
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
    taken              int unsigned not null,
    failed_msg         text null
);

create table subscribe_event_failed
(
    id                 bigint unsigned auto_increment
        primary key,
    subscribe_event_id bigint unsigned not null,
    failed_msg         text not null,
    create_time        bigint unsigned not null
);

insert into compensate_leader
    (name, term_start, term_end, transition_period, leader_id, version)
values ('publish_leader', 0, 0, 0, '', 0);

insert into compensate_leader
    (name, term_start, term_end, transition_period, leader_id, version)
values ('subscribe_leader', 0, 0, 0, '', 0);
