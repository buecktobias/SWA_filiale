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
package com.acme.filiale.graphql;

import java.util.List;
import java.util.Map;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationContext;
import org.springframework.graphql.ResponseError;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;
import static com.acme.filiale.config.dev.DevConfig.DEV;
import static com.acme.filiale.graphql.FilialeQueryTest.HOST;
import static com.acme.filiale.graphql.FilialeQueryTest.ID_PATTERN;
import static com.acme.filiale.graphql.FilialeQueryTest.SCHEMA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.condition.JRE.JAVA_18;
import static org.junit.jupiter.api.condition.JRE.JAVA_19;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@Tag("integration")
@Tag("graphql")
@Tag("mutation")
@DisplayName("GraphQL-Schnittstelle fuer Schreiben testen")
@ExtendWith(SoftAssertionsExtension.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles(DEV)
@EnabledForJreRange(min = JAVA_18, max = JAVA_19)
@SuppressWarnings("WriteTag")
class FilialeMutationTest {
    private final HttpGraphQlClient client;

    @InjectSoftAssertions
    private SoftAssertions softly;

    FilialeMutationTest(@LocalServerPort final int port, final ApplicationContext ctx) {
        final var getController = ctx.getBean(KundeMutationController.class);
        assertThat(getController).isNotNull();

        final var baseUrl = SCHEMA + "://" + HOST + ":" + port + "/graphql";
        final var webClient = WebClient.builder().build();
        client = HttpGraphQlClient
            .builder(webClient)
            .url(baseUrl)
            .build();
    }

    @Test
    @DisplayName("Neuanlegen einer neuen Filiale")
    void create() {
        // given
        final var mutation = """
            mutation {
                create(
                    input: {
                        name: "Neuernachname-Graphql"
                        email: "neue.email.graphql@acme.com"
                        kategorie: 1
                        hasNewsletter: true
                        geburtsdatum: "2022-01-31"
                        umsatz: {
                            betrag: "1"
                            waehrung: "EUR"
                        }
                        homepage: "https://test.de"
                        geschlecht: WEIBLICH
                        familienstand: LEDIG
                        interessen: [LESEN, REISEN]
                        adresse: {
                            plz: "12345"
                            ort: "Testort"
                        }
                    }
                ) {
                    id
                }
            }
            """;

        // when
        final var response = client
            .document(mutation)
            .execute()
            .block();

        // then
        assertThat(response).isNotNull();
        softly.assertThat(response.isValid()).isTrue();
        softly.assertThat(response.getErrors()).isEmpty();

        final var id = response.field("create.id").toEntity(String.class);
        assertThat(id).matches(ID_PATTERN);
    }

    @Test
    @DisplayName("Neuanlegen mit ungueltigen Werten")
    void createInvalid() {
        // given
        final var paths = List.of(
            "input.name",
            "input.email",
            "input.kategorie",
            "input.interessen",
            "input.adresse.plz"
        );

        final var mutation = """
            mutation {
                create(
                    input: {
                        name: "?!$"
                        email: "email@"
                        kategorie: 11
                        hasNewsletter: true
                        geburtsdatum: "2022-01-31"
                        homepage: "https://test.de"
                        geschlecht: WEIBLICH
                        familienstand: LEDIG
                        interessen: [SPORT, SPORT]
                        umsatz: {
                            betrag: "1"
                            waehrung: "EUR"
                        }
                        adresse: {
                            plz: "1234"
                            ort: "Testort"
                        }
                    }
                ) {
                    id
                }
            }
            """;

        // when
        final var response = client
            .document(mutation)
            .execute()
            .block();

        // then
        assertThat(response).isNotNull();
        assertThat(response.isValid()).isTrue();
        assertThat((Map<?, ?>) response.getData()).isNotNull().isEmpty();

        final var errors = response.getErrors();
        assertThat(errors).isNotEmpty().hasSize(paths.size());
        errors.forEach(error -> {
            final var errorType = error.getErrorType();
            softly.assertThat(errorType).isEqualTo(ErrorType.BAD_REQUEST);
        });

        final var pathsActual = errors.stream()
            .map(ResponseError::getPath)
            .toList();
        assertThat(pathsActual)
            .hasSameSizeAs(paths)
            .hasSameElementsAs(paths);
    }
}
