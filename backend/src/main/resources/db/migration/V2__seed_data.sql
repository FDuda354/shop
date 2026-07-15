INSERT INTO categories (id, name, description, slug) VALUES (1, 'GRAINS', '', 'grains');
INSERT INTO categories (id, name, description, slug) VALUES (2, 'DAIRY', '', 'dairy');
INSERT INTO categories (id, name, description, slug) VALUES (3, 'FRUITS', '', 'fruits');
INSERT INTO categories (id, name, description, slug) VALUES (4, 'VEGETABLES', '', 'vegetables');
INSERT INTO categories (id, name, description, slug) VALUES (5, 'MEAT', '', 'meat');

INSERT INTO products(name, category_id, description, full_description, price, currency, image, slug) VALUES
('Apple', 3, 'Apple description', 'Apple full description', 3.00, 'PLN', 'apple.gif', 'apple'),
('Orange', 3, 'Orange description', 'Orange full description', 5.00, 'PLN', 'loader.gif', 'orange'),
('Banana', 3, 'Banana description', 'Banana full description', 4.40, 'PLN', 'loader.gif', 'banana'),
('Potato', 4, 'Potato description', 'Potato full description', 1.20, 'PLN', 'loader.gif', 'potato'),
('Tomato', 4, 'Tomato description', 'Tomato full description', 7.00, 'PLN', 'loader.gif', 'tomato'),
('Onion', 4, 'Onion description', 'Onion full description', 1.70, 'PLN', 'loader.gif', 'onion'),
('Milk', 2, 'Milk description', 'Milk full description', 2.70, 'PLN', 'loader.gif', 'milk'),
('Cheese', 2, 'Cheese description', 'Cheese full description', 20.50, 'PLN', 'loader.gif', 'cheese'),
('Butter', 2, 'Butter description', 'Butter full description', 7.00, 'PLN', 'loader.gif', 'butter'),
('Pork', 5, 'Pork description', 'Pork full description', 16.00, 'PLN', 'loader.gif', 'pork'),
('Steak', 5, 'Steak description', 'Steak full description', 50.00, 'PLN', 'loader.gif', 'steak'),
('Bread', 1, 'Bread description', 'Bread full description', 5.00, 'PLN', 'loader.gif', 'bread'),
('Cereals', 1, 'Cereals description', 'Cereals full description', 8.00, 'PLN', 'loader.gif', 'cereals'),
('Chips', 1, 'Chips description', 'Chips full description', 18.00, 'PLN', 'chips.gif', 'chips'),
('Duck', 5, 'Duck description', 'Duck full description', 30.00, 'PLN', 'duck.gif', 'duck'),
('Turkey', 5, 'Turkey description', 'Turkey full description', 35.00, 'PLN', 'turkey.gif', 'turkey'),
('Pizza', 1, 'Pizza description', 'Pizza full description', 20.00, 'PLN', 'pizza.gif', 'pizza'),
('Wather', 4, 'Wather description', 'Wather full description', 2.00, 'PLN', 'wather.gif', 'wather');

INSERT INTO users (id, username, password, enabled)
VALUES (1, 'admin', '{bcrypt}$2a$10$upzXFsFUOClFRR69OMKF8eajGMRs0vhcSHqvNDKy9yfW45w7o9z6O', true);
INSERT INTO authorities (username, authority)
VALUES ('admin', 'ROLE_ADMIN');

INSERT INTO shipments(name, price, type, default_shipment) VALUES ('Courier', 14.99, 'COURIER', true);
INSERT INTO shipments(name, price, type, default_shipment) VALUES ('Pick up', 0.0, 'PICKUP', false);

INSERT INTO payments(id, name, type, default_payment, note)
VALUES (1, 'Bank Transfer', 'BANK_TRANSFER', true, 'Prosimy o dokonanie przelewu na konto:\n30 1030 1739 5825 1518 9904 4499\n w tytule proszę podać nr zamówienia');

-- Seedy z jawnymi ID nie przesuwają sekwencji identity — bez synchronizacji
-- pierwszy INSERT z aplikacji (np. /register) dostałby id=1 i wpadł na PK.
SELECT setval(pg_get_serial_sequence('categories', 'id'), (SELECT MAX(id) FROM categories));
SELECT setval(pg_get_serial_sequence('users', 'id'), (SELECT MAX(id) FROM users));
SELECT setval(pg_get_serial_sequence('payments', 'id'), (SELECT MAX(id) FROM payments));
