INSERT INTO filiale.umsatz (id, betrag, waehrung)
VALUES (UUID_TO_BIN('10000000-0000-0000-0000-000000000000'), 0, 'EUR');
INSERT INTO filiale.umsatz (id, betrag, waehrung)
VALUES (UUID_TO_BIN('10000000-0000-0000-0000-000000000001'), 10, 'EUR');
INSERT INTO filiale.umsatz (id, betrag, waehrung)
VALUES (UUID_TO_BIN('10000000-0000-0000-0000-000000000002'), 20, 'USD');
INSERT INTO filiale.umsatz (id, betrag, waehrung)
VALUES (UUID_TO_BIN('10000000-0000-0000-0000-000000000030'), 30, 'CHF');
INSERT INTO filiale.umsatz (id, betrag, waehrung)
VALUES (UUID_TO_BIN('10000000-0000-0000-0000-000000000040'), 40, 'GBP');

INSERT INTO filiale.adresse (id, plz, ort)
VALUES (0, '00000', 'Aachen');
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

-- admin
INSERT INTO kunde (id, version, nachname, email, kategorie, has_newsletter, geburtsdatum, homepage, geschlecht,
                   familienstand, umsatz_id, adresse_id, username, erzeugt, aktualisiert)
VALUES (UUID_TO_BIN('00000000-0000-0000-0000-000000000000'), 0, 'Admin', 'admin@acme.com', 0, true, '2022-01-31',
        'https://www.acme.com', 'W', 'VH', UUID_TO_BIN('10000000-0000-0000-0000-000000000000'),
        UUID_TO_BIN('20000000-0000-0000-0000-000000000000'), 'admin', '2022-01-31 00:00:00', '2022-01-31 00:00:00');
-- HTTP GET
INSERT INTO kunde (id, version, nachname, email, kategorie, has_newsletter, geburtsdatum, homepage, geschlecht,
                   familienstand, umsatz_id, adresse_id, username, erzeugt, aktualisiert)
VALUES (UUID_TO_BIN('00000000-0000-0000-0000-000000000001'), 0, 'Alpha', 'alpha@acme.de', 1, true, '2022-01-01',
        'https://www.acme.de', 'M', 'L', UUID_TO_BIN('10000000-0000-0000-0000-000000000001'),
        UUID_TO_BIN('20000000-0000-0000-0000-000000000001'), 'alpha', '2022-01-01 00:00:00', '2022-01-01 00:00:00');
INSERT INTO kunde (id, version, nachname, email, kategorie, has_newsletter, geburtsdatum, homepage, geschlecht,
                   familienstand, umsatz_id, adresse_id, username, erzeugt, aktualisiert)
VALUES (UUID_TO_BIN('00000000-0000-0000-0000-000000000002'), 0, 'Alpha', 'alpha@acme.edu', 2, true, '2022-01-02',
        'https://www.acme.edu', 'W', 'G', UUID_TO_BIN('10000000-0000-0000-0000-000000000002'),
        UUID_TO_BIN('20000000-0000-0000-0000-000000000002'), 'alpha2', '2022-01-02 00:00:00', '2022-01-02 00:00:00');
-- HTTP PUT
INSERT INTO kunde (id, version, nachname, email, kategorie, has_newsletter, geburtsdatum, homepage, geschlecht,
                   familienstand, umsatz_id, adresse_id, username, erzeugt, aktualisiert)
VALUES (UUID_TO_BIN('00000000-0000-0000-0000-000000000030'), 0, 'Alpha', 'alpha@acme.ch', 3, true, '2022-01-03',
        'https://www.acme.ch', 'M', 'VW', UUID_TO_BIN('10000000-0000-0000-0000-000000000030'),
        UUID_TO_BIN('20000000-0000-0000-0000-000000000030'), 'alpha3', '2022-01-03 00:00:00', '2022-01-03 00:00:00');
-- HTTP PATCH
INSERT INTO kunde (id, version, nachname, email, kategorie, has_newsletter, geburtsdatum, homepage, geschlecht,
                   familienstand, umsatz_id, adresse_id, username, erzeugt, aktualisiert)
VALUES (UUID_TO_BIN('00000000-0000-0000-0000-000000000040'), 0, 'Delta', 'delta@acme.uk', 4, true, '2022-01-04',
        'https://www.acme.uk', 'W', 'VH', UUID_TO_BIN('10000000-0000-0000-0000-000000000040'),
        UUID_TO_BIN('20000000-0000-0000-0000-000000000040'), 'delta', '2022-01-04 00:00:00', '2022-01-04 00:00:00');
-- HTTP DELETE
INSERT INTO kunde (id, version, nachname, email, kategorie, has_newsletter, geburtsdatum, homepage, geschlecht,
                   familienstand, umsatz_id, adresse_id, username, erzeugt, aktualisiert)
VALUES (UUID_TO_BIN('00000000-0000-0000-0000-000000000050'), 0, 'Epsilon', 'epsilon@acme.jp', 5, true, '2022-01-05',
        'https://www.acme.jp', 'M', 'L', null, UUID_TO_BIN('20000000-0000-0000-0000-000000000050'), 'epsilon',
        '2022-01-05 00:00:00', '2022-01-05 00:00:00');
-- zur freien Verfuegung
INSERT INTO kunde (id, version, nachname, email, kategorie, has_newsletter, geburtsdatum, homepage, geschlecht,
                   familienstand, umsatz_id, adresse_id, username, erzeugt, aktualisiert)
VALUES (UUID_TO_BIN('00000000-0000-0000-0000-000000000060'), 0, 'Phi', 'phi@acme.cn', 6, true, '2022-01-06',
        'https://www.acme.cn', 'D', 'L', null, UUID_TO_BIN('20000000-0000-0000-0000-000000000060'), 'phi',
        '2022-01-06 00:00:00', '2022-01-06 00:00:00');

