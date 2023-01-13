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
package com.acme.filiale.service;

import com.acme.filiale.entity.Filiale;
import com.acme.filiale.repository.FilialenDBRepository;
import com.acme.filiale.repository.FilialenRepository;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

/**
 * Anwendungslogik für filialen auch mit Bean Validation.
 * <img src="../../../../../asciidoc/KundeWriteService.svg" alt="Klassendiagramm">
 *
 * @author <a href="mailto:Juergen.Zimmermann@h-ka.de">Jürgen Zimmermann</a>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public final class FilialeWriteService {
    private final FilialenDBRepository repo;

    private final Validator validator;

    /**
     * Einen neuen filialen anlegen.
     *
     * @param filiale Das Objekt des neu anzulegenden filialen.
     * @return Der neu angelegte filialen mit generierter ID
     * @throws ConstraintViolationsException Falls mindestens ein Constraint verletzt ist.
     * @throws EmailExistsException Es gibt bereits einen filialen mit der Emailadresse.
     */
    public Filiale create(@Valid final Filiale filiale) {
        log.debug("create: {}", filiale);

        final var violations = validator.validate(filiale);
        if (!violations.isEmpty()) {
            log.debug("create: violations={}", violations);
            throw new ConstraintViolationsException(violations);
        }

        if (repo.existsByEmail(filiale.getEmail())) {
            throw new EmailExistsException(filiale.getEmail());
        }

        final var filialeDb = repo.save(filiale);
        log.debug("create: {}", filialeDb);
        return filialeDb;
    }

    /**
     * Einen vorhandenen Filiale aktualisieren.
     *
     * @param filiale Das Objekt mit den neuen Daten (ohne ID)
     * @param id ID des zu aktualisierenden Filiale
     * @throws ConstraintViolationsException Falls mindestens ein Constraint verletzt ist.
     * @throws NotFoundException Keine Filiale zur ID vorhanden.
     * @throws EmailExistsException Es gibt bereits eine Filiale mit der Emailadresse.
     */
    public void update(final Filiale filiale, final Long id) {
        log.debug("update: {}", filiale);
        log.debug("update: id={}", id);

        final var violations = validator.validate(filiale);
        if (!violations.isEmpty()) {
            log.debug("update: violations={}", violations);
            throw new ConstraintViolationsException(violations);
        }

        final var kundeDbOptional = repo.findById(id);
        if (kundeDbOptional.isEmpty()) {
            throw new NotFoundException(id);
        }

        final var email = filiale.getEmail();
        final var kundeDb = kundeDbOptional.get();
        // Ist die neue Email bei einer *ANDEREN* Filiale  vorhanden?
        if (!Objects.equals(email, kundeDb.getEmail()) && repo.existsByEmail(email)) {
            log.debug("update: email {} existiert", email);
            throw new EmailExistsException(email);
        }

        filiale.setId(id);
        repo.save(filiale);
    }

    /**
     * Eine vorhandene Filiale löschen.
     *
     * @param id Die ID des zu löschenden Filiale.
     */
    public void deleteById(final Long id) {
        log.debug("deleteById: id={}", id);
        repo.deleteById(id);
    }
}
