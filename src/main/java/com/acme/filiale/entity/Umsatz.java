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
package com.acme.filiale.entity;

import java.math.BigDecimal;
import java.util.Currency;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

/**
 * Geldbetrag und Währungseinheit für eine Umsatzangabe.
 *
 * @author <a href="mailto:Juergen.Zimmermann@h-ka.de">Jürgen Zimmermann</a>
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
@Builder
@AllArgsConstructor
@SuppressWarnings({"JavadocDeclaration", "RequireEmptyLineBeforeBlockTagGroup"})
public class Umsatz {

    @Id
    private Integer id;


    /**
     * Der Betrag beim Umsatz.
     * @param betrag Der Betrag.
     * @return Der Betrag.
     */
    private BigDecimal betrag;

    /**
     * Die Währung beim Umsatz.
     * @param waehrung Die Währung.
     * @return Die Währung.
     */
    private Currency waehrung;

}
