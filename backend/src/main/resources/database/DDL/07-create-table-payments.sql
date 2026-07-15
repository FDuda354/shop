--liquibase formatted sql
--changeset FDuda:11
CREATE TABLE payments (
  id serial PRIMARY KEY,
  name varchar(64) NOT NULL,
  type varchar(32) NOT NULL,
  default_payment boolean DEFAULT false,
  note text
);

