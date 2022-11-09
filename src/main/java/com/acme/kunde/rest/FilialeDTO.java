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
package com.acme.kunde.rest;

import com.acme.kunde.entity.Adresse;
import com.acme.kunde.entity.FamilienstandType;
import com.acme.kunde.entity.GeschlechtType;
import com.acme.kunde.entity.InteresseType;
import com.acme.kunde.entity.Kunde;
import com.acme.kunde.entity.Umsatz;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;

/**
 * ValueObject für das Neuanlegen und Ändern einer neuen Filiale. Beim Lesen wird die Klasse FilialeModel für die Ausgabe
 * verwendet.
 *
 * @param name Gültiger Nachname eines Kunden, d.h. mit einem geeigneten Muster.
 * @param email Email der Filiale.
 * @param homepage Die Homepage der Filiale.
 * @param umsatz Der Umsatz der Filiale.
 * @param adresse Die Adresse der Filiale.
 */
@SuppressWarnings("RecordComponentNumber")
record FilialeDTO(
    String name,
    String email,
    URL homepage,
    Umsatz umsatz,
    Adresse adresse
) {
    /**
     * Konvertierung in ein Objekt des Anwendungskerns.
     *
     * @return Kundeobjekt für den Anwendungskern
     */
    Kunde toKunde() {
        return Kunde
            .builder()
            .id(null)
            .nachname(name)
            .email(email)
            .homepage(homepage)
            .umsatz(umsatz)
            .adresse(adresse)
            .build();
    }
}
