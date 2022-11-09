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
package com.acme.filiale.repository;

import com.acme.filiale.entity.Adresse;
import com.acme.filiale.entity.Filiale;
import com.acme.filiale.entity.Umsatz;
import lombok.SneakyThrows;

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Currency;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.math.BigDecimal.ZERO;
import static java.util.Locale.GERMANY;

/**
 * Emulation der Datenbasis für persistente Kunden.
 */
@SuppressWarnings({"UtilityClassCanBeEnum", "UtilityClass", "MagicNumber", "RedundantSuppression"})
final class DB {
    /**
     * Liste der Kunden zur Emulation der DB.
     */
    @SuppressWarnings("StaticCollection")
    static final List<Filiale> FILIALEN = getFilialen();

    private DB() {
    }

    @SneakyThrows(MalformedURLException.class)
    @SuppressWarnings({"FeatureEnvy", "TrailingComment"})
    private static List<Filiale> getFilialen() {
        final var currencyGermany = Currency.getInstance(GERMANY);
        return Stream.of(
                Filiale.builder()
                    .id(UUID.fromString("00000000-0000-0000-0000-000000000000"))
                    .name("Admin")
                    .email("admin@acme.com")
                    .homepage(new URL("https://www.acme.com"))
                    .umsatz(Umsatz.builder().betrag(ZERO).waehrung(currencyGermany).build())
                    .adresse(Adresse.builder().plz("00000").ort("Aachen").build())
                    .build(),
                Filiale.builder()
                    .id(UUID.fromString("00000000-0000-0000-0000-000000000001"))
                    .name("Alpha")
                    .email("alpha@acme.de")
                    .homepage(new URL("https://www.acme.de"))
                    .umsatz(Umsatz.builder().betrag(new BigDecimal("10")).waehrung(currencyGermany).build())
                    .adresse(Adresse.builder().plz("11111").ort("Augsburg").build())
                    .build(),
                Filiale.builder()
                    .id(UUID.fromString("00000000-0000-0000-0000-000000000002"))
                    .name("Alpha")
                    .email("alpha@acme.edu")
                    .homepage(new URL("https://www.acme.edu"))
                    .umsatz(Umsatz.builder().betrag(new BigDecimal("20")).waehrung(currencyGermany).build())
                    .adresse(Adresse.builder().plz("22222").ort("Aalen").build())
                    .build(),
                Filiale.builder()
                    .id(UUID.fromString("00000000-0000-0000-0000-000000000030"))
                    .name("Alpha")
                    .email("alpha@acme.ch")
                    .homepage(new URL("https://www.acme.ch"))
                    .umsatz(Umsatz.builder().betrag(new BigDecimal("30")).waehrung(currencyGermany).build())
                    .adresse(Adresse.builder().plz("33333").ort("Ahlen").build())
                    .build(),
                Filiale.builder()
                    .id(UUID.fromString("00000000-0000-0000-0000-000000000040"))
                    .name("Delta")
                    .email("delta@acme.uk")
                    .homepage(new URL("https://www.acme.uk"))
                    .umsatz(Umsatz.builder().betrag(new BigDecimal("40")).waehrung(currencyGermany).build())
                    .adresse(Adresse.builder().plz("44444").ort("Dortmund").build())
                    .build(),
                Filiale.builder()
                    .id(UUID.fromString("00000000-0000-0000-0000-000000000050"))
                    .name("Epsilon")
                    .email("epsilon@acme.jp")
                    .homepage(new URL("https://www.acme.jp"))
                    .umsatz(Umsatz.builder().betrag(new BigDecimal("50")).waehrung(currencyGermany).build())
                    .adresse(Adresse.builder().plz("55555").ort("Essen").build())
                    .build(),
                Filiale.builder()
                    .id(UUID.fromString("00000000-0000-0000-0000-000000000060"))
                    .name("Phi")
                    .email("phi@acme.cn")
                    .homepage(new URL("https://www.acme.cn"))
                    .umsatz(Umsatz.builder().betrag(new BigDecimal("60")).waehrung(currencyGermany).build())
                    .adresse(Adresse.builder().plz("66666").ort("Freiburg").build())
                    .build()
            )
            .collect(Collectors.toList());
    }
}
