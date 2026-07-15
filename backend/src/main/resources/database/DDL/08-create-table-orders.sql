--liquibase formatted sql
--changeset FDuda:12
CREATE TABLE orders (
    id serial PRIMARY KEY,
    place_date timestamp NOT NULL,
    order_status varchar(32) NOT NULL,
    gross_value numeric(15,2) NOT NULL,
    first_name varchar(30) NOT NULL,
    last_name varchar(30) NOT NULL,
    street varchar(60) NOT NULL,
    city varchar(30) NOT NULL,
    zip_code varchar(10) NOT NULL,
    email varchar(60) NOT NULL,
    phone varchar(20) NOT NULL,
    payment_id bigint,
    user_id bigint,
    CONSTRAINT fk_payment_id FOREIGN KEY (payment_id) REFERENCES payments(id),
    CONSTRAINT fk_user_id FOREIGN KEY (user_id) REFERENCES users(id)

);

CREATE TABLE order_rows (
    id serial PRIMARY KEY,
    order_id bigint NOT NULL,
    product_id bigint ,
    shipment_id bigint ,
    quantity int NOT NULL,
    price numeric(15,2) NOT NULL,
    CONSTRAINT fk_order_id FOREIGN KEY (order_id) REFERENCES orders(id),
    CONSTRAINT fk_shipment_id FOREIGN KEY (shipment_id) REFERENCES shipments(id),
    CONSTRAINT fk_product_id FOREIGN KEY (product_id) REFERENCES products(id)

);

