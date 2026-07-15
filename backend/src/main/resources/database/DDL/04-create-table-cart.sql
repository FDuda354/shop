--liquibase formatted sql
--changeset FDuda:5
CREATE TABLE baskets (
  id serial PRIMARY KEY,
  created timestamp NOT NULL
);
--changeset FDuda:6
CREATE TABLE basket_items (
  id serial PRIMARY KEY,
  product_id bigint NOT NULL,
  quantity int,
  basket_id bigint NOT NULL,
  CONSTRAINT fk_product_id FOREIGN KEY (product_id) REFERENCES products(id),
  CONSTRAINT fk_basket_id FOREIGN KEY (basket_id) REFERENCES baskets(id)
);