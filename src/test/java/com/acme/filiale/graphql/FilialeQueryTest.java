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

import java.util.Collection;
import java.util.Map;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;
import static com.acme.filiale.config.dev.DevConfig.DEV;
import static com.acme.filiale.entity.Adresse.PLZ_PATTERN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.condition.JRE.JAVA_18;
import static org.junit.jupiter.api.condition.JRE.JAVA_19;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@Tag("integration")
@Tag("graphql")
@Tag("query")
@DisplayName("GraphQL-Schnittstelle fuer Lesen testen")
@ExtendWith(SoftAssertionsExtension.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles(DEV)
@EnabledForJreRange(min = JAVA_18, max = JAVA_19)
@SuppressWarnings({"WriteTag", "MissingJavadoc"})
class FilialeQueryTest {
    static final String SCHEMA = "http";
    static final String HOST = "localhost";
    static final String ID_PATTERN =
        "[\\dA-Fa-f]{8}-[\\dA-Fa-f]{4}-[\\dA-Fa-f]{4}-[\\dA-Fa-f]{4}-[\\dA-Fa-f]{12}";

    private final HttpGraphQlClient client;

    @InjectSoftAssertions
    private SoftAssertions softly;

    FilialeQueryTest(@LocalServerPort final int port, final ApplicationContext ctx) {
        final var getController = ctx.getBean(KundeQueryController.class);
        assertThat(getController).isNotNull();

        final var baseUrl = SCHEMA + "://" + HOST + ":" + port + "/graphql";
        final var webClient = WebClient.builder().build();
        client = HttpGraphQlClient
            .builder(webClient)
            .url(baseUrl)
            .build();
    }

    @Test
    @DisplayName("Suche nach allen Kunden")
    void findAll() {
        // given
        final var query = """
            {
                kunden(input: {}) {
                    id
                    name
                    email
                }
            }
            """;

        // when
        final var response = client
            .document(query)
            .execute()
            .block();

        // then
        assertThat(response).isNotNull();
        softly.assertThat(response.isValid()).isTrue();
        softly.assertThat(response.getErrors()).isEmpty();

        final Collection<Map<String, String>> kunden = response.field("kunden").getValue();
        assertThat(kunden).isNotEmpty();
        final var anzahl = kunden.size();
        for (int i = 0; i < anzahl; i++) {
            final var id = response.field("kunden[%d].id".formatted(i)).toEntity(String.class);
            softly.assertThat(id).matches(ID_PATTERN);
            final var nachname = response.field("kunden[%d].name".formatted(i)).toEntity(String.class);
            softly.assertThat(nachname).matches(NACHNAME_PATTERN);
            final var email = response.field("kunden[%d].email".formatted(i)).toEntity(String.class);
            softly.assertThat(email).contains("@");
        }
    }

    @Test
    @DisplayName("Suche mit vorhandenem Nachnamen")
    void findByNachname() {
        // given
        final var query = """
            {
                kunden(input: {nachname: "Alpha"}) {
                    id
                    email
                }
            }
            """;

        // when
        final var response = client
            .document(query)
            .execute()
            .block();

        // then
        assertThat(response).isNotNull();
        assertThat(response.isValid()).isTrue();
        assertThat(response.getErrors()).isEmpty();

        final Collection<Map<String, String>> kunden = response.field("kunden").getValue();
        assertThat(kunden).isNotEmpty();
        final var anzahl = kunden.size();
        for (int i = 0; i < anzahl; i++) {
            final var id = response.field("kunden[%d].id".formatted(i)).toEntity(String.class);
            softly.assertThat(id).matches(ID_PATTERN);
            final var email = response.field("kunden[%d].email".formatted(i)).toEntity(String.class);
            softly.assertThat(email).contains("@");
        }
    }

    @Test
    @DisplayName("Suche mit nicht-vorhandenem Nachnamen")
    void findByNachnameNichtVorhanden() {
        // given
        final var query = """
            {
                kunden(input: {nachname: "Nichtvorhanden"}) {
                    id
                }
            }
            """;

        // when
        final var response = client
            .document(query)
            .execute()
            .block();

        // then
        assertThat(response).isNotNull();
        assertThat(response.isValid()).isTrue();
        assertThat((Map<?, ?>) response.getData()).isNotNull().isEmpty();

        final var errors = response.getErrors();
        assertThat(errors).hasSize(1);
        final var error = errors.get(0);
        final var errorType = error.getErrorType();
        assertThat(errorType).isEqualTo(ErrorType.NOT_FOUND);
    }

    @Test
    @DisplayName("Suche mit vorhandener Email")
    void findByEmail() {
        // given
        final var query = """
            {
                kunden(input: {email: "admin@acme.com"}) {
                    id
                    nachname
                }
            }
            """;

        // when
        final var response = client
            .document(query)
            .execute()
            .block();

        // then
        assertThat(response).isNotNull();
        assertThat(response.isValid()).isTrue();
        assertThat(response.getErrors()).isEmpty();

        final Collection<Map<String, String>> kunden = response.field("kunden").getValue();
        assertThat(kunden).isNotEmpty();
        final var anzahl = kunden.size();
        for (int i = 0; i < anzahl; i++) {
            final var id = response.field("kunden[%d].id".formatted(i)).toEntity(String.class);
            softly.assertThat(id).matches(ID_PATTERN);
            final var nachname = response.field("kunden[%d].nachname".formatted(i)).toEntity(String.class);
            softly.assertThat(nachname).matches(NACHNAME_PATTERN);
        }
    }

    @Test
    @DisplayName("Suche mit nicht-vorhandener Email")
    void findByEmailNichtVorhanden() {
        // given
        final var query = """
            {
                kunden(input: {email: "nicht.vorhanden@acme.com"}) {
                    id
                }
            }
            """;

        // when
        final var response = client
            .document(query)
            .execute()
            .block();

        // then
        assertThat(response).isNotNull();
        assertThat(response.isValid()).isTrue();
        assertThat((Map<?, ?>) response.getData()).isNotNull().isEmpty();

        final var errors = response.getErrors();
        assertThat(errors).hasSize(1);
        final var error = errors.get(0);
        final var errorType = error.getErrorType();
        assertThat(errorType).isEqualTo(ErrorType.NOT_FOUND);
    }

    @Nested
    @DisplayName("Suche anhand der ID")
    class FindById {
        @Test
        @DisplayName("Suche mit vorhandener ID")
        void findById() {
            // given
            final var query = """
                {
                    filiale(id: "00000000-0000-0000-0000-000000000001") {
                        nachname
                        email
                        adresse {
                            plz
                        }
                    }
                }
                """;

            // when
            final var response = client
                .document(query)
                .execute()
                .block();

            // then
            assertThat(response).isNotNull();
            softly.assertThat(response.isValid()).isTrue();
            softly.assertThat(response.getErrors()).isEmpty();

            final var nachname = response.field("filiale.nachname").toEntity(String.class);
            softly.assertThat(nachname)
                .isNotEmpty()
                .matches(NACHNAME_PATTERN);

            final var email = response.field("filiale.email").toEntity(String.class);
            softly.assertThat(email)
                .isNotEmpty()
                .contains("@");

            final var plz = response.field("filiale.adresse.plz").toEntity(String.class);
            softly.assertThat(plz)
                .isNotEmpty()
                .matches(PLZ_PATTERN);
        }

        @Test
        @DisplayName("Suche mit nicht-vorhandener ID")
        void findByIdNichtVorhanden() {
            // given
            final var query = """
                {
                    filiale(id: "ffffffff-ffff-ffff-ffff-ffffffffffff") {
                        nachname
                    }
                }
                """;

            // when
            final var response = client
                .document(query)
                .execute()
                .block();

            // then
            assertThat(response).isNotNull();
            softly.assertThat(response.isValid()).isTrue();
            assertThat((Map<?, ?>) response.getData()).isNotNull().isEmpty();

            final var errors = response.getErrors();
            assertThat(errors).hasSize(1);
            final var error = errors.get(0);
            final var errorType = error.getErrorType();
            assertThat(errorType).isEqualTo(ErrorType.NOT_FOUND);
        }
    }
}
