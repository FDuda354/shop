--liquibase formatted sql
--changeset FDuda:3
INSERT INTO users (id, username, password, enabled)
VALUES (1, 'admin', '{bcrypt}$2a$10$upzXFsFUOClFRR69OMKF8eajGMRs0vhcSHqvNDKy9yfW45w7o9z6O', true);
INSERT INTO authorities (username, authority)
VALUES ('admin', 'ROLE_ADMIN');



