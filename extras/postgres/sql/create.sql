
CREATE TABLE IF NOT EXISTS filiale.filiale (
    id       bigint NOT NULL PRIMARY KEY,
    name VARCHAR(20) UNIQUE NOT NULL,
    email VARCHAR(50) NOT NULL ,
    homepage VARCHAR(50),
    umsatz_id bigint,
    adresse_id bigint
);

CREATE TABLE filiale.umsatz (
    id        bigint NOT NULL PRIMARY KEY,
    betrag    DECIMAL(10,2) NOT NULL,
    waehrung  CHAR(3) NOT NULL
);

CREATE TABLE IF NOT EXISTS filiale.adresse (
    id    bigint PRIMARY KEY,
    plz   CHAR(5) NOT NULL,
    ort   VARCHAR(40) NOT NULL
);

ALTER TABLE filiale.filiale ADD FOREIGN KEY (umsatz_id) REFERENCES filiale.umsatz(id);
ALTER TABLE filiale.filiale ADD FOREIGN KEY (adresse_id) REFERENCES filiale.adresse(id);

