-- Tłumaczenia danych: kolumny bazowe (name/description/full_description) są
-- polskie, kolumny *_en trzymają opcjonalną wersję angielską. Brak wartości EN
-- oznacza fallback do polskiej — rozstrzyga to frontend (utils/localized.ts).
ALTER TABLE products
    ADD COLUMN name_en varchar(255),
    ADD COLUMN description_en varchar(100),
    ADD COLUMN full_description_en text;

ALTER TABLE categories
    ADD COLUMN name_en varchar(255),
    ADD COLUMN description_en text;

-- Dotychczasowe treści były po angielsku — wędrują do kolumn EN, żeby nic nie
-- zginęło także w bazach z danymi dodanymi już po seedach.
-- NULLIF: pusty string ma być brakiem tłumaczenia (NULL), nie "pustym EN" —
-- admin zapisywał full_description jako '' przy braku pełnego opisu.
UPDATE products
SET name_en             = name,
    description_en      = description,
    full_description_en = NULLIF(full_description, '');

UPDATE categories
SET name_en        = name,
    description_en = NULLIF(description, '');

-- Polskie wersje znanych seedów (V2). Wiersze spoza seedów zostają z tą samą
-- treścią po obu stronach — admin może je potem rozdzielić w panelu.
UPDATE categories SET name = 'ZBOŻA' WHERE slug = 'grains';
UPDATE categories SET name = 'NABIAŁ' WHERE slug = 'dairy';
UPDATE categories SET name = 'OWOCE' WHERE slug = 'fruits';
UPDATE categories SET name = 'WARZYWA' WHERE slug = 'vegetables';
UPDATE categories SET name = 'MIĘSO' WHERE slug = 'meat';

UPDATE products SET name = 'Jabłko',              description = 'Jabłko — krótki opis',              full_description = 'Jabłko — pełny opis'              WHERE slug = 'apple';
UPDATE products SET name = 'Pomarańcza',          description = 'Pomarańcza — krótki opis',          full_description = 'Pomarańcza — pełny opis'          WHERE slug = 'orange';
UPDATE products SET name = 'Banan',               description = 'Banan — krótki opis',               full_description = 'Banan — pełny opis'               WHERE slug = 'banana';
UPDATE products SET name = 'Ziemniak',            description = 'Ziemniak — krótki opis',            full_description = 'Ziemniak — pełny opis'            WHERE slug = 'potato';
UPDATE products SET name = 'Pomidor',             description = 'Pomidor — krótki opis',             full_description = 'Pomidor — pełny opis'             WHERE slug = 'tomato';
UPDATE products SET name = 'Cebula',              description = 'Cebula — krótki opis',              full_description = 'Cebula — pełny opis'              WHERE slug = 'onion';
UPDATE products SET name = 'Mleko',               description = 'Mleko — krótki opis',               full_description = 'Mleko — pełny opis'               WHERE slug = 'milk';
UPDATE products SET name = 'Ser',                 description = 'Ser — krótki opis',                 full_description = 'Ser — pełny opis'                 WHERE slug = 'cheese';
UPDATE products SET name = 'Masło',               description = 'Masło — krótki opis',               full_description = 'Masło — pełny opis'               WHERE slug = 'butter';
UPDATE products SET name = 'Wieprzowina',         description = 'Wieprzowina — krótki opis',         full_description = 'Wieprzowina — pełny opis'         WHERE slug = 'pork';
UPDATE products SET name = 'Stek',                description = 'Stek — krótki opis',                full_description = 'Stek — pełny opis'                WHERE slug = 'steak';
UPDATE products SET name = 'Chleb',               description = 'Chleb — krótki opis',               full_description = 'Chleb — pełny opis'               WHERE slug = 'bread';
UPDATE products SET name = 'Płatki śniadaniowe', description = 'Płatki śniadaniowe — krótki opis', full_description = 'Płatki śniadaniowe — pełny opis' WHERE slug = 'cereals';
UPDATE products SET name = 'Chipsy',              description = 'Chipsy — krótki opis',              full_description = 'Chipsy — pełny opis'              WHERE slug = 'chips';
UPDATE products SET name = 'Kaczka',              description = 'Kaczka — krótki opis',              full_description = 'Kaczka — pełny opis'              WHERE slug = 'duck';
UPDATE products SET name = 'Indyk',               description = 'Indyk — krótki opis',               full_description = 'Indyk — pełny opis'               WHERE slug = 'turkey';
UPDATE products SET name = 'Pizza',               description = 'Pizza — krótki opis',               full_description = 'Pizza — pełny opis'               WHERE slug = 'pizza';
UPDATE products SET name = 'Woda',                description = 'Woda — krótki opis',                full_description = 'Woda — pełny opis'                WHERE slug = 'wather';
