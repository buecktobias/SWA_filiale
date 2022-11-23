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
package com.acme.filiale.rest;

import com.acme.filiale.rest.patch.FilialePatcher;
import com.acme.filiale.rest.patch.InvalidPatchOperationException;
import com.acme.filiale.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.acme.filiale.rest.FilialeGetController.ID_PATTERN;
import static com.acme.filiale.rest.UriHelper.getBaseUri;
import static com.acme.filiale.rest.UriHelper.getRequestUri;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.*;

/**
 * Eine `@RestController`-Klasse bildet die REST-Schnittstelle, wobei die HTTP-Methoden, Pfade und MIME-Typen auf die
 * Methoden der Klasse abgebildet werden.
 * <img src="../../../../../asciidoc/KundeWriteController.svg" alt="Klassendiagramm">
 *
 * @author <a href="mailto:Juergen.Zimmermann@h-ka.de">Jürgen Zimmermann</a>
 */
@RestController
@RequestMapping("/")
@Tag(name = "filiale API")
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("ClassFanOutComplexity")
final class FilialeWriteController {
    @SuppressWarnings("TrailingComment")
    private static final String PROBLEM_PATH = "/problem/";

    private final FilialeWriteService writeService;

    /**
     * Einen neuen filiale-Datensatz anlegen.
     *
     * @param filialeDTO Das filialenobjekt aus dem eingegangenen Request-Body.
     * @param request    Das Request-Objekt, um `Location` im Response-Header zu erstellen.
     * @return Response mit Statuscode 201 einschließlich Location-Header oder Statuscode 422 falls Constraints verletzt
     * sind oder die Emailadresse bereits existiert oder Statuscode 400 falls syntaktische Fehler im Request-Body
     * vorliegen.
     * @throws URISyntaxException falls die URI im Request-Objekt nicht korrekt wäre
     */
    @PostMapping(path = "/create", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Eine neuen filialen anlegen", tags = "Neuanlegen")
    @ApiResponse(responseCode = "201", description = "filiale neu angelegt")
    @ApiResponse(responseCode = "400", description = "Syntaktische Fehler im Request-Body")
    @ApiResponse(responseCode = "422", description = "Ungültige Werte oder Email vorhanden")
    @SuppressWarnings("TrailingComment")
    ResponseEntity<String> create(
        @RequestBody final FilialeDTO filialeDTO,
        final HttpServletRequest request
    ) throws URISyntaxException {
        log.debug("create: {}", filialeDTO);

        final var filiale = writeService.create(filialeDTO.toFiliale());
        final var baseUri = getBaseUri(request);
        log.debug(filiale.toString());
        final var location = new URI(baseUri + "/" + filiale.getId());
        return created(location).build();
    }

    /**
     * Einen vorhandenen filiale-Datensatz überschreiben.
     *
     * @param id         ID des zu aktualisierenden filialen.
     * @param filialeDTO Das filialenobjekt aus dem eingegangenen Request-Body.
     * @return Response mit Statuscode 204 oder Statuscode 422, falls Constraints verletzt sind oder
     * der JSON-Datensatz syntaktisch nicht korrekt ist oder falls die Emailadresse bereits existiert oder
     * Statuscode 400 falls syntaktische Fehler im Request-Body vorliegen.
     */
    @PutMapping(path = "{id:" + ID_PATTERN + "}", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Eine Filiale mit neuen Werten aktualisieren", tags = "Aktualisieren")
    @ApiResponse(responseCode = "204", description = "Aktualisiert")
    @ApiResponse(responseCode = "400", description = "Syntaktische Fehler im Request-Body")
    @ApiResponse(responseCode = "404", description = "filiale nicht vorhanden")
    @ApiResponse(responseCode = "422", description = "Ungültige Werte oder Email vorhanden")
    ResponseEntity<Void> update(
        @PathVariable final UUID id,
        @RequestBody final FilialeDTO filialeDTO
    ) {
        log.debug("update: id={}, {}", id, filialeDTO);
        writeService.update(filialeDTO.toFiliale(), id);
        return noContent().build();
    }

    @ExceptionHandler(ConstraintViolationsException.class)
    @SuppressWarnings("unused")
    ResponseEntity<ProblemDetail> handleConstraintViolations(
        final ConstraintViolationsException ex,
        final HttpServletRequest request
    ) {
        log.debug("handleConstraintViolations: {}", ex.getMessage());

        final var kundeViolations = ex.getViolations()
            .stream()
            .map(violation -> violation.getPropertyPath() + ": " +
                violation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName() + " " +
                violation.getMessage())
            .collect(Collectors.toList());
        log.trace("handleConstraintViolations: {}", kundeViolations);
        final String detail;
        if (kundeViolations.isEmpty()) {
            detail = "N/A";
        } else {
            // [ und ] aus dem String der Liste entfernen
            final var violationsStr = kundeViolations.toString();
            detail = violationsStr.substring(1, violationsStr.length() - 2);
        }

        final var problemDetail = ProblemDetail.forStatusAndDetail(UNPROCESSABLE_ENTITY, detail);
        problemDetail.setType(URI.create(PROBLEM_PATH + ProblemType.CONSTRAINTS.getValue()));
        final var uri = getRequestUri(request);
        problemDetail.setInstance(uri);

        return ResponseEntity.of(problemDetail).build();
    }

    @ExceptionHandler(EmailExistsException.class)
    @SuppressWarnings("unused")
    ResponseEntity<ProblemDetail> handleEmailExists(final EmailExistsException ex, final HttpServletRequest request) {
        log.debug("handleEmailExists: {}", ex.getMessage());
        final var problemDetail = ProblemDetail.forStatusAndDetail(UNPROCESSABLE_ENTITY, ex.getMessage());
        problemDetail.setType(URI.create(PROBLEM_PATH + ProblemType.CONSTRAINTS.getValue()));
        final var uri = getRequestUri(request);
        problemDetail.setInstance(uri);
        return ResponseEntity.of(problemDetail).build();
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @SuppressWarnings("unused")
    ResponseEntity<ProblemDetail> handleMessageNotReadable(
        final HttpMessageNotReadableException ex,
        final HttpServletRequest request
    ) {
        log.debug("handleMessageNotReadable: {}", ex.getMessage());
        final var problemDetail = ProblemDetail.forStatusAndDetail(BAD_REQUEST, ex.getMessage());
        problemDetail.setType(URI.create(PROBLEM_PATH + ProblemType.BAD_REQUEST.getValue()));
        final var uri = getRequestUri(request);
        problemDetail.setInstance(uri);
        return ResponseEntity.of(problemDetail).build();
    }

    @ExceptionHandler(NotFoundException.class)
    @SuppressWarnings("unused")
    ResponseEntity<String> handleNotFound(final NotFoundException ex) {
        log.debug("handleNotFound: {}", ex.getMessage());
        return notFound().build();
    }

    @ExceptionHandler(InvalidPatchOperationException.class)
    @SuppressWarnings("unused")
    ResponseEntity<ProblemDetail> handleInvalidPatchOperation(
        final InvalidPatchOperationException ex,
        final HttpServletRequest request
    ) {
        log.debug("handleInvalidPatchOperation: {}", ex.getMessage());
        final var problemDetail = ProblemDetail.forStatusAndDetail(UNPROCESSABLE_ENTITY, ex.getMessage());
        problemDetail.setType(URI.create(PROBLEM_PATH + ProblemType.UNPROCESSABLE.getValue()));
        final var uri = getRequestUri(request);
        problemDetail.setInstance(uri);
        return ResponseEntity.of(problemDetail).build();
    }
}
