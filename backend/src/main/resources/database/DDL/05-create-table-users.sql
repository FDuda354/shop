--liquibase formatted sql
--changeset FDuda:7
CREATE TABLE users (
id serial PRIMARY KEY,
username varchar(50) NOT NULL UNIQUE,
password varchar(500) NOT NULL,
enabled boolean NOT NULL,
image varchar(255),
hash VARCHAR(120),
hash_date TIMESTAMP
);
--changeset FDuda:8
CREATE TABLE authorities (
username varchar(50) NOT NULL,
authority varchar(50) NOT NULL,
CONSTRAINT fk_authorities_users FOREIGN KEY (username) REFERENCES users (username)
);
--changeset FDuda:9
CREATE UNIQUE INDEX ix_auth_username ON authorities (username, authority);

