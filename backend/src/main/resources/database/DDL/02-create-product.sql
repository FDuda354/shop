--liquibase formatted sql
--changeset FDuda:2
CREATE TABLE products (
    id serial PRIMARY KEY,
    name varchar(255) NOT NULL,
    category_id BIGINT NOT NULL,
    description varchar(100) NOT NULL,
    full_description text,
    price numeric(9,2) NOT NULL,
    currency varchar(3) NOT NULL,
    image varchar(255),
    slug varchar(255),
CONSTRAINT fk_categories FOREIGN KEY (category_id) REFERENCES categories(id)

);
--changeset FDuda:3
ALTER TABLE products ADD CONSTRAINT uq_product_slug UNIQUE (slug);