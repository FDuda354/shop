--liquibase formatted sql
--changeset FDuda:4
INSERT INTO shipments(name, price, type, default_shipment) VALUES ('Courier', 14.99, 'COURIER', true);
INSERT INTO shipments(name, price, type, default_shipment) VALUES ('Pick up', 0.0, 'PICKUP', false);


