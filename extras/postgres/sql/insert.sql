TRUNCATE filiale.umsatz, filiale.adresse, filiale.filiale;
INSERT INTO filiale.umsatz (id, betrag, waehrung)
VALUES (0, 0, 'EUR');
INSERT INTO filiale.umsatz (id, betrag, waehrung)
VALUES (1, 10, 'EUR');
INSERT INTO filiale.umsatz (id, betrag, waehrung)
VALUES (2, 20, 'USD');
INSERT INTO filiale.umsatz (id, betrag, waehrung)
VALUES (30, 30, 'CHF');
INSERT INTO filiale.umsatz (id, betrag, waehrung)
VALUES (40, 40, 'GBP');

INSERT INTO filiale.adresse (id, plz, ort) VALUES (0, '00000', 'Aachen');
INSERT INTO filiale.adresse (id, plz, ort)
VALUES (1, '11111', 'Augsburg');
INSERT INTO filiale.adresse (id, plz, ort)
VALUES (2, '22222', 'Aalen');
INSERT INTO filiale.adresse (id, plz, ort)
VALUES (30, '33333', 'Ahlen');
INSERT INTO filiale.adresse (id, plz, ort)
VALUES (40, '44444', 'Dortmund');
INSERT INTO filiale.adresse (id, plz, ort)
VALUES (50, '55555', 'Essen');
INSERT INTO filiale.adresse (id, plz, ort)
VALUES (60, '66666', 'Freiburg');

INSERT INTO filiale.filiale(id, name, email, homepage, umsatz_id, adresse_id)
VALUES (0, 'Admin', 'admin@acme.com', 'https://www.acme.com', 0, 0);

INSERT INTO filiale.filiale(id, name, email, homepage, umsatz_id, adresse_id)
VALUES (1, 'Alpha', 'alpha@acme.de', 'https://www.acme.de', 1, 1);

INSERT INTO filiale.filiale(id, name, email, homepage, umsatz_id, adresse_id)
VALUES (2, 'Alpha2', 'alpha@acme.edu', 'https://www.acme.edu', 2, 2);

INSERT INTO filiale.filiale(id, name, email, homepage, umsatz_id, adresse_id)
VALUES (3, 'Alpha3', 'alpha@acme.ch', 'https://www.acme.ch', 30, 30);

INSERT INTO filiale.filiale(id, name, email, homepage, umsatz_id, adresse_id)
VALUES (4, 'Delta', 'delta@acme.uk', 'https://www.acme.uk', 40, 40);

INSERT INTO filiale.filiale(id, name, email, homepage, umsatz_id, adresse_id)
VALUES (5, 'Epsilon', 'epsilon@acme.jp', 'https://www.acme.jp', null, 1);

INSERT INTO filiale.filiale(id, name, email, homepage, umsatz_id, adresse_id)
VALUES (6, 'Phi', 'phi@acme.cn', 'https://www.acme.cn', null, 60);

