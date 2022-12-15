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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.net.URL;
import java.util.UUID;

import static jakarta.persistence.CascadeType.PERSIST;
import static jakarta.persistence.CascadeType.REMOVE;
import static jakarta.persistence.FetchType.LAZY;

/**
 * Daten einer Filiale. In DDD ist Kunde ist ein Aggregate Root.
 * <img src="../../../../../extras/doc/FilialeUML-0.png" alt="Klassendiagramm">
 *
 * @author <a href="mailto:Juergen.Zimmermann@h-ka.de">Jürgen Zimmermann</a>
 */
// https://thorben-janssen.com/java-records-hibernate-jpa
@Builder
@Entity
@Table(name="filiale")
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings({"ClassFanOutComplexity", "JavadocDeclaration", "RequireEmptyLineBeforeBlockTagGroup"})
public class Filiale {
    /**
     * Die ID der Filiale.
     *
     * @param id Die ID.
     * @return Die ID.
     */
    @EqualsAndHashCode.Include
    @Id
    private Long id;

    /**
     * Der Name der Filiale.
     *
     * @param name Der Name.
     * @return Der Name.
     */
    @NotEmpty
    private String name;

    /**
     * Die Emailadresse der Filiale.
     *
     * @param email Die Emailadresse.
     * @return Die Emailadresse.
     */
    @Email
    @NotNull
    private String email;
    /**
     * Die URL zur Homepage der Filiale.
     *
     * @param homepage Die URL zur Homepage.
     * @return Die URL zur Homepage.
     */
    private URL homepage;
    /**
     * Der Umsatz der Filiale.
     *
     * @param umsatz Der Umsatz.
     * @return Der Umsatz.
     */
    @ToString.Exclude
    @OneToOne(cascade = {PERSIST, REMOVE}, fetch = LAZY)
    @JoinColumn(updatable = false)
    private Umsatz umsatz;

    /**
     * Die Adresse der Filiale.
     *
     * @param adresse Die Adresse.
     * @return Die Adresse.
     */
    @Valid
    @ToString.Exclude
    @OneToOne(cascade = {PERSIST, REMOVE}, fetch = LAZY)
    @JoinColumn(updatable = false)
    private Adresse adresse;
}
