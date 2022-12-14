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
package com.acme.filiale.graphql;

import com.acme.filiale.service.ConstraintViolationsException;
import com.acme.filiale.service.EmailExistsException;
import com.acme.filiale.service.NotFoundException;
import graphql.GraphQLError;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Abbildung von Exceptions auf GraphQLError.
 *
 * @author <a href="mailto:Juergen.Zimmermann@h-ka.de">Jürgen Zimmermann</a>
 */
@Component
final class ExceptionHandler extends DataFetcherExceptionResolverAdapter {
    /**
     * Abbildung der Exceptions auf GraphQLError.
     *
     * @param ex  Exception aus dem Anwendungskern
     * @param env Environment-Objekt
     */
    @Override
    @SuppressWarnings("ReturnCount")
    protected GraphQLError resolveToSingleError(
        final Throwable ex,
        @SuppressWarnings("NullableProblems") final DataFetchingEnvironment env
    ) {
        // "Pattern Matching for instanceof" ab Java 14
        //noinspection ChainOfInstanceofChecks
        if (ex instanceof final NotFoundException notFound) {
            return new NotFoundError(notFound.getId(), notFound.getSuchkriterien());
        } else if (ex instanceof EmailExistsException emailExists) {
            return new EmailExistsError(emailExists.getEmail());
        } else if (ex instanceof DateTimeParseException dateTimeParse) {
            return new DateTimeParseError(dateTimeParse.getParsedString());
        }
        return super.resolveToSingleError(ex, env);

        // Pattern matching ab Java 18 (Preview) https://openjdk.org/jeps/433
        // switch (ex) {
        //     case final NotFoundException notFound -> {
        //         return new NotFoundError(notFound.getId(), notFound.getSuchkriterien());
        //     }
        //     case final EmailExistsException emailExists -> {
        //         return new EmailExistsError(emailExists.getEmail());
        //     }
        //     case final DateTimeParseException dateTimeParse -> {
        //         return new DateTimeParseError(dateTimeParse.getParsedString());
        //     }
        //     default -> {
        //         return super.resolveToSingleError(ex, env);
        //     }
        // }
    }

    /**
     * Abbildung der Exceptions aus KundeGraphQlController auf GraphQLError.
     *
     * @param ex  Exception aus KundeGraphQlController
     * @param env Environment-Objekt
     */
    @Override
    protected List<GraphQLError> resolveToMultipleErrors(
        final Throwable ex,
        @SuppressWarnings("NullableProblems") final DataFetchingEnvironment env
    ) {
        if (ex instanceof final ConstraintViolationsException cve) {
            return cve.getViolations()
                .stream()
                .map(ConstraintViolationError::new)
                .collect(Collectors.toList());
        }

        return super.resolveToMultipleErrors(ex, env);
    }
}
