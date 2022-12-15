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

import com.acme.filiale.entity.Filiale;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.UUID.randomUUID;

/**
 * Repository für den DB-Zugriff bei Filialen.
 *
 * @author <a href="mailto:Juergen.Zimmermann@h-ka.de">Jürgen Zimmermann</a>
 */
@Repository
@Slf4j
@SuppressWarnings("PublicConstructor")
public class FilialenRepository {
    public final FilialenDBRepository filialenDBRepository;

    public FilialenRepository(FilialenDBRepository filialenDBRepository) {
        this.filialenDBRepository = filialenDBRepository;
    }
    public List<Filiale> getAllFromDB(){
        return this.filialenDBRepository.findAll();
    }


    /**
     * Eine Filiale anhand seiner ID suchen.
     *
     * @param id Die Id der gesuchten Filiale
     * @return Optional mit der gefundenen Filiale oder leeres Optional
     */
    public Optional<Filiale> findById(final Long id) {
        log.debug("findById: id={}", id);
        final var result = this.getAllFromDB().stream()
            .filter(filiale -> Objects.equals(filiale.getId(), id))
            .findFirst();
        log.debug("findById: {}", result);
        return result;
    }

    /**
     * Filiale anhand von Suchkriterien ermitteln.
     * Z.B. mit GET https://localhost:8080/api?name=A&amp;plz=7
     *
     * @param suchkriterien Suchkriterien.
     * @return Gefundene Filialen oder leere Collection.
     */
    @SuppressWarnings({"ReturnCount", "JavadocLinkAsPlainText"})
    public @NonNull Collection<Filiale> find(final Map<String, String> suchkriterien) {
        log.debug("find: suchkriterien={}", suchkriterien);

        if (suchkriterien.isEmpty()) {
            return findAll();
        }

        for (final var entry : suchkriterien.entrySet()) {
            switch (entry.getKey()) {
                case "email" -> {
                    final var result = findByEmail(entry.getValue());
                    //noinspection OptionalIsPresent
                    return result.isPresent() ? List.of(result.get()) : Collections.emptyList();
                }
                case "name" -> {
                    return findByName(entry.getValue());
                }
                default -> log.debug("find: ungueltiges Suchkriterium={}", entry.getKey());
            }
        }

        return Collections.emptyList();
    }

    /**
     * Alle Filialen als Collection ermitteln, wie sie später auch von der DB kommen.
     *
     * @return Alle Filialen
     */
    public @NonNull Collection<Filiale> findAll() {
        return this.getAllFromDB();
    }

    /**
     * Kunde zu gegebener Emailadresse aus der DB ermitteln.
     *
     * @param email Emailadresse für die Suche
     * @return Gefundener Kunde oder leeres Optional
     */
    public Optional<Filiale> findByEmail(final String email) {
        log.debug("findByEmail: {}", email);
        final var result = this.getAllFromDB().stream()
            .filter(filiale -> Objects.equals(filiale.getEmail(), email))
            .findFirst();
        log.debug("findByEmail: {}", result);
        return result;
    }

    /**
     * Abfrage, ob es eine Filiale mit gegebener Emailadresse gibt.
     *
     * @param email Emailadresse für die Suche
     * @return true, falls es einen solchen Filialen gibt, sonst false
     */
    public boolean isEmailExisting(final String email) {
        log.debug("isEmailExisting: email={}", email);
        final var count = this.getAllFromDB().stream()
            .filter(kunde -> Objects.equals(kunde.getEmail(), email))
            .count();
        log.debug("isEmailExisting: count={}", count);
        return count > 0L;
    }

    /**
     * Filiale anhand des Namens suchen.
     *
     * @param name Der (Teil-) Name der gesuchten Filiale
     * @return Die gefundenen Filialen oder eine leere Collection
     */
    public @NonNull Collection<Filiale> findByName(final CharSequence name) {
        log.debug("findByName: name={}", name);
        final var filialen = this.getAllFromDB().stream()
            .filter(kunde -> kunde.getName().contains(name))
            .collect(Collectors.toList());
        log.debug("findByNamen: filialen={}", filialen);
        return filialen;
    }

    /**
     * Abfrage, welchen Namen es zu einem Präfix gibt.
     *
     * @param prefix Namen-Präfix.
     * @return Die passenden Namen oder eine leere Collection.
     */
    public @NonNull Collection<String> findNamenByPrefix(final @NonNull String prefix) {
        log.debug("findByName: prefix={}", prefix);
        final var namen = this.getAllFromDB().stream()
            .map(Filiale::getName)
            .filter(name -> name.startsWith(prefix))
            .distinct()
            .collect(Collectors.toList());
        log.debug("findByName: namen={}", namen);
        return namen;
    }

    /**
     * Einen neuen Filialen anlegen.
     *
     * @param filiale Das Objekt des neu anzulegenden Filialen.
     * @return Der neu angelegte Kunde mit generierter ID
     */
    public @NonNull Filiale create(final @NonNull Filiale filiale) {
        log.debug("create: {}", filiale);
        final var randomLong = new Random().nextLong();
        filiale.setId(randomLong);
        this.getAllFromDB().add(filiale);
        log.debug("create: {}", filiale);
        return filiale;
    }

    /**
     * Einen vorhandenen Filiale aktualisieren.
     *
     * @param filiale Das Objekt mit den neuen Daten
     */
    public void update(final @NonNull Filiale filiale) {
        log.debug("update: {}", filiale);
        final OptionalInt index = IntStream
            .range(0, this.getAllFromDB().size())
            .filter(i -> Objects.equals(this.getAllFromDB().get(i).getId(), filiale.getId()))
            .findFirst();
        log.trace("update: index={}", index);
        if (index.isEmpty()) {
            return;
        }
        this.getAllFromDB().set(index.getAsInt(), filiale);
        log.debug("update: {}", filiale);
    }

    /**
     * Einen vorhandenen Filiale löschen.
     *
     * @param id Die ID des zu löschenden Filiale.
     */
    public void deleteById(final Long id) {
        log.debug("deleteById: id={}", id);
        final OptionalInt index = IntStream
            .range(0, this.getAllFromDB().size())
            .filter(i -> Objects.equals(this.getAllFromDB().get(i).getId(), id))
            .findFirst();
        log.trace("deleteById: index={}", index);
        index.ifPresent(this.getAllFromDB()::remove);
        log.debug("deleteById: #Filiale={}", this.getAllFromDB().size());
    }
}
