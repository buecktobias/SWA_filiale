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
    private final FilialenRepository repo;

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

        if (repo.isEmailExisting(filiale.getEmail())) {
            throw new EmailExistsException(filiale.getEmail());
        }

        final var kundeDB = repo.create(filiale);
        log.debug("create: {}", kundeDB);
        return kundeDB;
    }

    /**
     * Einen vorhandenen Kunden aktualisieren.
     *
     * @param filiale Das Objekt mit den neuen Daten (ohne ID)
     * @param id ID des zu aktualisierenden Kunden
     * @throws ConstraintViolationsException Falls mindestens ein Constraint verletzt ist.
     * @throws NotFoundException Kein Kunde zur ID vorhanden.
     * @throws EmailExistsException Es gibt bereits einen Kunden mit der Emailadresse.
     */
    public void update(final Filiale filiale, final UUID id) {
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
        // Ist die neue Email bei einem *ANDEREN* Kunden vorhanden?
        if (!Objects.equals(email, kundeDb.getEmail()) && repo.isEmailExisting(email)) {
            log.debug("update: email {} existiert", email);
            throw new EmailExistsException(email);
        }

        filiale.setId(id);
        repo.update(filiale);
    }

    /**
     * Einen vorhandenen Kunden löschen.
     *
     * @param id Die ID des zu löschenden Kunden.
     */
    public void deleteById(final UUID id) {
        log.debug("deleteById: id={}", id);
        repo.deleteById(id);
    }
}
