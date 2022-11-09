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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.acme.filiale.repository.DB.KUNDEN;
import static java.util.UUID.randomUUID;

/**
 * Repository für den DB-Zugriff bei Kunden.
 *
 * @author <a href="mailto:Juergen.Zimmermann@h-ka.de">Jürgen Zimmermann</a>
 */
@Repository
@Slf4j
@SuppressWarnings("PublicConstructor")
public final class FilialenRepository {
    /**
     * Eine Filiale anhand seiner ID suchen.
     *
     * @param id Die Id der gesuchten Filiale
     * @return Optional mit der gefundenen Filiale oder leeres Optional
     */
    public Optional<Filiale> findById(final UUID id) {
        log.debug("findById: id={}", id);
        final var result = KUNDEN.stream()
            .filter(kunde -> Objects.equals(kunde.getId(), id))
            .findFirst();
        log.debug("findById: {}", result);
        return result;
    }

    /**
     * Filiale anhand von Suchkriterien ermitteln.
     * Z.B. mit GET https://localhost:8080/api?nachname=A&amp;plz=7
     *
     * @param suchkriterien Suchkriterien.
     * @return Gefundene Kunden oder leere Collection.
     */
    @SuppressWarnings({"ReturnCount", "JavadocLinkAsPlainText"})
    public @NonNull Collection<Filiale> find(final Map<String, String> suchkriterien) {
        log.debug("find: suchkriterien={}", suchkriterien);

        if (suchkriterien.isEmpty()) {
            return findAll();
        }

        // for-Schleife statt Higher-order Function "forEach" wegen return
        for (final var entry : suchkriterien.entrySet()) {
            switch (entry.getKey()) {
                case "email" -> {
                    final var result = findByEmail(entry.getValue());
                    //noinspection OptionalIsPresent
                    return result.isPresent() ? List.of(result.get()) : Collections.emptyList();
                }
                case "nachname" -> {
                    return findByName(entry.getValue());
                }
                default -> log.debug("find: ungueltiges Suchkriterium={}", entry.getKey());
            }
        }

        return Collections.emptyList();
    }

    /**
     * Alle Kunden als Collection ermitteln, wie sie später auch von der DB kommen.
     *
     * @return Alle Kunden
     */
    public @NonNull Collection<Filiale> findAll() {
        return KUNDEN;
    }

    /**
     * Kunde zu gegebener Emailadresse aus der DB ermitteln.
     *
     * @param email Emailadresse für die Suche
     * @return Gefundener Kunde oder leeres Optional
     */
    public Optional<Filiale> findByEmail(final String email) {
        log.debug("findByEmail: {}", email);
        final var result = KUNDEN.stream()
            .filter(kunde -> Objects.equals(kunde.getEmail(), email))
            .findFirst();
        log.debug("findByEmail: {}", result);
        return result;
    }

    /**
     * Abfrage, ob es einen Kunden mit gegebener Emailadresse gibt.
     *
     * @param email Emailadresse für die Suche
     * @return true, falls es einen solchen Kunden gibt, sonst false
     */
    public boolean isEmailExisting(final String email) {
        log.debug("isEmailExisting: email={}", email);
        final var count = KUNDEN.stream()
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
        final var kunden = KUNDEN.stream()
            .filter(kunde -> kunde.getName().contains(name))
            .collect(Collectors.toList());
        log.debug("findByNachname: kunden={}", kunden);
        return kunden;
    }

    /**
     * Abfrage, welche Nachnamen es zu einem Präfix gibt.
     *
     * @param prefix Nachname-Präfix.
     * @return Die passenden Nachnamen oder eine leere Collection.
     */
    public @NonNull Collection<String> findNachnamenByPrefix(final @NonNull String prefix) {
        log.debug("findByNachname: prefix={}", prefix);
        final var nachnamen = KUNDEN.stream()
            .map(Filiale::getName)
            .filter(nachname -> nachname.startsWith(prefix))
            .distinct()
            .collect(Collectors.toList());
        log.debug("findByNachname: nachnamen={}", nachnamen);
        return nachnamen;
    }

    /**
     * Einen neuen Kunden anlegen.
     *
     * @param filiale Das Objekt des neu anzulegenden Kunden.
     * @return Der neu angelegte Kunde mit generierter ID
     */
    public @NonNull Filiale create(final @NonNull Filiale filiale) {
        log.debug("create: {}", filiale);
        filiale.setId(randomUUID());
        KUNDEN.add(filiale);
        log.debug("create: {}", filiale);
        return filiale;
    }

    /**
     * Einen vorhandenen Kunden aktualisieren.
     *
     * @param filiale Das Objekt mit den neuen Daten
     */
    public void update(final @NonNull Filiale filiale) {
        log.debug("update: {}", filiale);
        final OptionalInt index = IntStream
            .range(0, KUNDEN.size())
            .filter(i -> Objects.equals(KUNDEN.get(i).getId(), filiale.getId()))
            .findFirst();
        log.trace("update: index={}", index);
        if (index.isEmpty()) {
            return;
        }
        KUNDEN.set(index.getAsInt(), filiale);
        log.debug("update: {}", filiale);
    }

    /**
     * Einen vorhandenen Kunden löschen.
     *
     * @param id Die ID des zu löschenden Kunden.
     */
    public void deleteById(final UUID id) {
        log.debug("deleteById: id={}", id);
        final OptionalInt index = IntStream
            .range(0, KUNDEN.size())
            .filter(i -> Objects.equals(KUNDEN.get(i).getId(), id))
            .findFirst();
        log.trace("deleteById: index={}", index);
        index.ifPresent(KUNDEN::remove);
        log.debug("deleteById: #KUNDEN={}", KUNDEN.size());
    }
}
