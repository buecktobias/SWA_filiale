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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.acme.filiale.rest.patch;

import com.acme.filiale.entity.InteresseType;
import com.acme.filiale.entity.Filiale;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.acme.filiale.rest.patch.PatchOperationType.ADD;
import static com.acme.filiale.rest.patch.PatchOperationType.REMOVE;
import static com.acme.filiale.rest.patch.PatchOperationType.REPLACE;

/**
 * Klasse, um PATCH-Operationen auf Kunde-Objekte anzuwenden.
 *
 * @author <a href="mailto:Juergen.Zimmermann@h-ka.de">Jürgen Zimmermann</a>
 */
@Component
@Slf4j
public final class FilialePatcher {
    FilialePatcher() {
    }

    /**
     * PATCH-Operationen werden auf ein Kunde-Objekt angewandt.
     *
     * @param filiale Das zu modifizierende Kunde-Objekt.
     * @param operations Die anzuwendenden Operationen.
     * @throws InvalidPatchOperationException Falls die Patch-Operation nicht korrekt ist.
     */
    public void patch(final Filiale filiale, final Collection<PatchOperation> operations) {
        final var replaceOps = operations.stream()
            .filter(op -> op.op() == REPLACE)
            .collect(Collectors.toList());
        log.debug("patch: replaceOps={}", replaceOps);
        replaceOps(filiale, replaceOps);

        final var addOps = operations.stream()
            .filter(op -> op.op() == ADD)
            .collect(Collectors.toList());
        log.debug("patch: addOps={}", addOps);
        addInteressen(filiale, addOps);

        final var removeOps = operations.stream()
            .filter(op -> op.op() == REMOVE)
            .collect(Collectors.toList());
        log.debug("patch: removeOps={}", removeOps);
        removeInteressen(filiale, removeOps);
    }

    private void replaceOps(final Filiale filiale, final Iterable<PatchOperation> ops) {
        ops.forEach(op -> {
            switch (op.path()) {
                case "/nachname" -> filiale.setName(op.value());
                case "/email" -> filiale.setEmail(op.value());
                default -> throw new InvalidPatchOperationException();
            }
        });
        log.trace("replaceOps: kunde={}", filiale);
    }

    private void addInteressen(final Filiale filiale, final Collection<PatchOperation> ops) {
        if (ops.isEmpty()) {
            return;
        }
        ops.stream()
            .filter(op -> Objects.equals("/interessen", op.path()))
            .forEach(op -> addInteresse(filiale, op));
        log.trace("addInteressen: kunde={}", filiale);
    }

    private void addInteresse(final Filiale filiale, final PatchOperation op) {
        final var value = op.value();
        final var interesseOpt = InteresseType.of(value);
        if (interesseOpt.isEmpty()) {
            throw new InvalidPatchOperationException();
        }
        final var interesse = interesseOpt.get();
        final var interessen = filiale.getInteressen() == null
            ? new ArrayList<InteresseType>(InteresseType.values().length)
            : new ArrayList<>(filiale.getInteressen());
        if (interessen.contains(interesse)) {
            throw new InvalidPatchOperationException();
        }
        interessen.add(interesse);
        log.trace("addInteresse: op={}, interessen={}", op, interessen);
        filiale.setInteressen(interessen);
    }

    private void removeInteressen(final Filiale filiale, final Collection<PatchOperation> ops) {
        if (filiale.getInteressen() == null) {
            throw new InvalidPatchOperationException();
        }
        if (ops.isEmpty()) {
            return;
        }
        ops.stream()
            .filter(op -> Objects.equals("/interessen", op.path()))
            .forEach(op -> removeInteresse(filiale, op));
    }

    private void removeInteresse(final Filiale filiale, final PatchOperation op) {
        final var interesseValue = op.value();
        final var interesseRemoveOpt = InteresseType.of(interesseValue);
        if (interesseRemoveOpt.isEmpty()) {
            throw new InvalidPatchOperationException();
        }
        final var interesseRemove = interesseRemoveOpt.get();
        final var interessen = filiale.getInteressen()
            .stream()
            .filter(interesse -> interesse != interesseRemove)
            .collect(Collectors.toList());
        filiale.setInteressen(interessen);
    }
}
