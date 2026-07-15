--liquibase formatted sql
--changeset FDuda:6
-- Seedy DML wstawiają rekordy z jawnymi ID, co nie przesuwa sekwencji serial.
-- Bez tej synchronizacji pierwszy INSERT z aplikacji (np. /register) dostaje
-- id=1 i wali w primary key. setval do MAX(id) jest bezpieczne także na bazie,
-- na której sekwencje już poszły do przodu.
SELECT setval(pg_get_serial_sequence('categories', 'id'), (SELECT MAX(id) FROM categories));
SELECT setval(pg_get_serial_sequence('users', 'id'), (SELECT MAX(id) FROM users));
SELECT setval(pg_get_serial_sequence('payments', 'id'), (SELECT MAX(id) FROM payments));
