-- Seed V2 wstawił w notce płatności dosłowne "\n" (dwa znaki) zamiast nowych
-- linii — frontend renderował backslash-n w UI. Podmieniamy na prawdziwe \n.
UPDATE payments SET note = replace(note, '\n', E'\n');
