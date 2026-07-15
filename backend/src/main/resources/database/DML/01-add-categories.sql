--liquibase formatted sql
--changeset FDuda:1
INSERT INTO categories (id, name, description, slug) VALUES (1, 'GRAINS', '', 'grains');
INSERT INTO categories (id, name, description, slug) VALUES (2, 'DAIRY', '', 'dairy');
INSERT INTO categories (id, name, description, slug) VALUES (3, 'FRUITS', '', 'fruits');
INSERT INTO categories (id, name, description, slug) VALUES (4, 'VEGETABLES', '', 'vegetables');
INSERT INTO categories (id, name, description, slug) VALUES (5, 'MEAT', '', 'meat');

