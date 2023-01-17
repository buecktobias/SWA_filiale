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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.Hibernate;

import java.net.URL;
import java.util.Objects;

import static jakarta.persistence.CascadeType.PERSIST;
import static jakarta.persistence.CascadeType.REMOVE;
import static jakarta.persistence.FetchType.LAZY;

/**
 * Daten einer Filiale. In DDD ist Kunde ist ein Aggregate Root.
 * <img src="../../../../../extras/doc/FilialeUML-0.png" alt="Klassendiagramm">
 */
@Builder
@Entity
@Table(name="filiale")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@SuppressWarnings({"ClassFanOutComplexity", "JavadocDeclaration", "RequireEmptyLineBeforeBlockTagGroup"})
public class Filiale {
    /**
     * Die ID der Filiale.
     *
     * @param id Die ID.
     * @return Die ID.
     */
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Filiale filiale = (Filiale) o;
        return id != null && Objects.equals(id, filiale.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
