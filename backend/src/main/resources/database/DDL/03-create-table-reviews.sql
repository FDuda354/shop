--liquibase formatted sql
--changeset FDuda:4
CREATE TABLE reviews (
    id serial PRIMARY KEY,
    product_id bigint NOT NULL ,
    user_id bigint,
    author_name varchar(60) NOT NULL,
    content text,
    CONSTRAINT fk_product_id FOREIGN KEY (product_id) REFERENCES products(id)
);