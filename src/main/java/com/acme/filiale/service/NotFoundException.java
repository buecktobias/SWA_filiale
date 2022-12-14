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

import java.util.Map;
import java.util.UUID;
import lombok.Getter;

/**
 * RuntimeException, falls kein filiale gefunden wurde.
 */
@Getter
@SuppressWarnings("ParameterHidesMemberVariable")
public final class NotFoundException extends RuntimeException {
    /**
     * Nicht-vorhandene ID.
     */
    private final UUID id;

    /**
     * Suchkriterien, zu denen nichts gefunden wurde.
     */
    private final Map<String, String> suchkriterien;

    NotFoundException(final UUID id) {
        super("Kein filiale mit der ID " + id + " gefunden.");
        this.id = id;
        //noinspection AssignmentToNull
        suchkriterien = null;
    }

    NotFoundException(final Map<String, String> suchkriterien) {
        super("Keine filialen gefunden.");
        //noinspection AssignmentToNull
        id = null;
        this.suchkriterien = suchkriterien;
    }

    @SuppressWarnings("AssignmentToNull")
    NotFoundException() {
        super("Keine filialen gefunden.");
        id = null;
        suchkriterien = null;
    }
}
