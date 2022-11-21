/*
 * Copyright (C) 2022 - present Juergen Zimmermann, Hochschule Karlsruhe
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.acme.filiale.graphql;

import com.acme.filiale.entity.Adresse;
import com.acme.filiale.entity.Filiale;
import com.acme.filiale.entity.Umsatz;
import java.net.URL;
import java.time.LocalDate;

/**
 * Eine Value-Klasse für Eingabedaten passend zu KundeInput aus dem GraphQL-Schema.
 *
 * @author <a href="mailto:Juergen.Zimmermann@h-ka.de">Jürgen Zimmermann</a>
 * @param name Nachname
 * @param email Emailadresse
 * @param geburtsdatum Geburtsdatum
 * @param homepage URL der Homepage
 * @param umsatz Umsatz
 * @param adresse Adresse
 */
@SuppressWarnings("RecordComponentNumber")
record FilialeInput(
    String name,
    String email,
    String geburtsdatum,
    URL homepage,
    UmsatzInput umsatz,
    AdresseInput adresse
) {
    /**
     * Konvertierung in ein Objekt der Entity-Klasse Kunde.
     *
     * @return Das konvertierte Kunde-Objekt
     */
    Filiale toFiliale() {
        final LocalDate geburtsdatumTmp;
        geburtsdatumTmp = LocalDate.parse(geburtsdatum);
        Umsatz umsatzTmp = null;
        if (umsatz != null) {
            umsatzTmp = Umsatz.builder().betrag(umsatz.betrag()).waehrung(umsatz.waehrung()).build();
        }
        final var adresseTmp = Adresse.builder().plz(adresse.plz()).ort(adresse.ort()).build();

        return Filiale
            .builder()
            .id(null)
            .email(email)
            .homepage(homepage)
            .umsatz(umsatzTmp)
            .adresse(adresseTmp)
            .build();
    }
}
