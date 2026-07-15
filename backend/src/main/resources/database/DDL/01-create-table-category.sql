--liquibase formatted sql
--changeset FDuda:1
CREATE TABLE categories (
    id serial PRIMARY KEY,
    name varchar(255) NOT NULL,
    description text,
    slug varchar(255) NOT NULL
);