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
package com.acme.filiale.rest;

import com.acme.filiale.rest.patch.PatchOperation;
import java.net.URI;
import java.net.URL;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.Currency;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import reactor.core.publisher.Mono;
import static com.acme.filiale.config.dev.DevConfig.DEV;
import static com.acme.filiale.entity.GeschlechtType.WEIBLICH;
import static com.acme.filiale.entity.InteresseType.LESEN;
import static com.acme.filiale.entity.InteresseType.REISEN;
import static com.acme.filiale.entity.InteresseType.SPORT;
import static com.acme.filiale.rest.FilialeGetController.ID_PATTERN;
import static com.acme.filiale.rest.patch.PatchOperationType.ADD;
import static com.acme.filiale.rest.patch.PatchOperationType.REMOVE;
import static com.acme.filiale.rest.patch.PatchOperationType.REPLACE;
import static com.acme.filiale.rest.FilialeGetRestTest.HOST;
import static com.acme.filiale.rest.FilialeGetRestTest.PASSWORD;
import static com.acme.filiale.rest.FilialeGetRestTest.SCHEMA;
import static com.acme.filiale.rest.FilialeGetRestTest.USER_ADMIN;
import static java.math.BigDecimal.ONE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.condition.JRE.JAVA_18;
import static org.junit.jupiter.api.condition.JRE.JAVA_19;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication;

@Tag("integration")
@Tag("rest")
@Tag("rest_write")
@DisplayName("REST-Schnittstelle fuer Schreiben testen")
@ExtendWith(SoftAssertionsExtension.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles(DEV)
@EnabledForJreRange(min = JAVA_18, max = JAVA_19)
@SuppressWarnings("WriteTag")
class FilialeWriteRestTest {
    private static final String ID_UPDATE_PUT = "00000000-0000-0000-0000-000000000030";
    private static final String ID_UPDATE_PATCH = "00000000-0000-0000-0000-000000000040";
    private static final String ID_DELETE = "00000000-0000-0000-0000-000000000050";

    private static final String NEUER_NACHNAME = "Neuernachname-Rest";
    private static final String NEUE_EMAIL = "email.rest@test.de";
    private static final String NEUE_EMAIL_PATCH = "email.rest@test.de.patch";
    private static final String NEUES_GEBURTSDATUM = "2022-01-31";
    private static final String CURRENCY_CODE = "EUR";
    private static final String NEUE_HOMEPAGE = "https://test.de";

    private static final InteresseType NEUES_INTERESSE = SPORT;
    private static final String NEUE_PLZ = "12345";
    private static final String NEUER_ORT = "Neuerortrest";

    private static final String NEUER_NACHNAME_INVALID = "?!$";
    private static final String NEUE_EMAIL_INVALID = "email@";
    private static final int NEUE_KATEGORIE_INVALID = 11;
    private static final String NEUES_GEBURTSDATUM_INVALID = "3000-01-31";
    private static final String NEUE_PLZ_INVALID = "1234";

    private static final InteresseType ZU_LOESCHENDES_INTERESSE = LESEN;

    private static final String ID_PATH = "{id}";

    private final WebClient client;
    private final KundeRepository kundeRepo;

    @InjectSoftAssertions
    private SoftAssertions softly;

    FilialeWriteRestTest(@LocalServerPort final int port, final ApplicationContext ctx) {
        final var writeController = ctx.getBean(FilialeWriteController.class);
        assertThat(writeController).isNotNull();

        final var baseUrl = SCHEMA + "://" + HOST + ":" + port;
        client = WebClient
            .builder()
            .filter(basicAuthentication(USER_ADMIN, PASSWORD))
            .baseUrl(baseUrl)
            .build();
        final var clientAdapter = WebClientAdapter.forClient(client);
        final var proxyFactory = HttpServiceProxyFactory
            .builder(clientAdapter)
            .build();
        kundeRepo = proxyFactory.createClient(KundeRepository.class);
    }

    @SuppressWarnings("DataFlowIssue")
    @Nested
    class Erzeugen {
        @ParameterizedTest(name = "[{index}] Neuanlegen eines neuen Kunden: nachname={0}, email={1}")
        @CsvSource(
            NEUER_NACHNAME + "," + NEUE_EMAIL + "," + NEUES_GEBURTSDATUM + "," + CURRENCY_CODE + "," + NEUE_HOMEPAGE +
                "," + NEUE_PLZ + "," + NEUER_ORT
        )
        @DisplayName("Neuanlegen eines neuen Kunden")
        void create(final ArgumentsAccessor args) {
            // given
            final var umsatz = new Umsatz(ONE, Currency.getInstance(args.get(3, String.class)));
            final var adresse = new Adresse(args.get(5, String.class), args.get(6, String.class));
            final var kunde = new KundeUpload(
                args.get(0, String.class),
                args.get(1, String.class),
                1,
                true,
                args.get(2, LocalDate.class),
                args.get(4, URL.class),
                WEIBLICH,
                null,
                java.util.List.of(LESEN, REISEN),
                umsatz,
                adresse
            );

            // when
            final var response = client
                .post()
                .contentType(APPLICATION_JSON)
                .bodyValue(kunde)
                .exchangeToMono(Mono::just)
                .block();


            // then
            assertThat(response).isNotNull();
            softly.assertThat(response.statusCode()).isEqualTo(CREATED);
            final var location = response.headers().asHttpHeaders().getLocation();
            softly.assertThat(location)
                .isNotNull()
                .isInstanceOf(URI.class);
            softly.assertThat(location.toString()).matches(".*/" + ID_PATTERN + "$");
        }

        @ParameterizedTest(name = "[{index}] Neuanlegen mit ungueltigen Werten: nachname={0}, email={1}")
        @CsvSource(
            NEUER_NACHNAME_INVALID + "," + NEUE_EMAIL_INVALID + "," + NEUE_KATEGORIE_INVALID + "," +
                NEUES_GEBURTSDATUM_INVALID + "," + NEUE_PLZ_INVALID + "," + NEUER_ORT
        )
        @DisplayName("Neuanlegen mit ungueltigen Werten")
        @SuppressWarnings("DynamicRegexReplaceableByCompiledPattern")
        void createInvalid(final ArgumentsAccessor args) {
            // given
            final var kunde = new KundeUpload(
                args.get(0, String.class),
                args.get(1, String.class),
                args.get(2, Integer.class),
                true,
                args.get(3, LocalDate.class),
                null,
                WEIBLICH,
                null,
                List.of(LESEN, REISEN, REISEN),
                null,
                new Adresse(args.get(4, String.class), args.get(5, String.class))
            );
            final var violationKeys = List.of(
                "nachname",
                "email",
                "kategorie",
                "geburtsdatum",
                "adresse.plz",
                "interessen"
            );

            // when
            final var body = client
                .post()
                .contentType(APPLICATION_JSON)
                .bodyValue(kunde)
                .exchangeToMono(response -> {
                    assertThat(response.statusCode()).isEqualTo(UNPROCESSABLE_ENTITY);
                    return response.bodyToMono(ProblemDetail.class);
                })
                .block();

            // then
            assertThat(body).isNotNull();
            final var detail = body.getDetail();
            assertThat(detail).isNotNull();
            final var violations = Arrays.asList(detail.split(", "));
            assertThat(violations).hasSameSizeAs(violationKeys);

            final var actualViolationKeys = violations
                .stream()
                // Keys vor ":" extrahieren
                .map(violation -> violation.split(": ")[0])
                .collect(Collectors.toList());
            assertThat(actualViolationKeys)
                .hasSameSizeAs(violationKeys)
                .hasSameElementsAs(violationKeys);
        }
    }

    @Nested
    class Aendern {
        @ParameterizedTest(name = "[{index}] Aendern eines vorhandenen Kunden durch PUT: id={0}")
        @ValueSource(strings = ID_UPDATE_PUT)
        @DisplayName("Aendern eines vorhandenen Kunden durch PUT")
        void put(final String id) {
            // given
            final var kundeOrig = kundeRepo.getFiliale(id).block();
            assertThat(kundeOrig).isNotNull();
            final var umsatzOrig = kundeOrig.umsatz();
            final var umsatz = (umsatzOrig == null)
                ? null
                : new Umsatz(umsatzOrig.betrag(), umsatzOrig.waehrung());
            final var adresse = new Adresse(kundeOrig.adresse().plz(), kundeOrig.adresse().ort());
            final var kunde = new KundeUpload(
                kundeOrig.nachname(),
                kundeOrig.email() + "put",
                kundeOrig.kategorie(),
                kundeOrig.hasNewsletter(),
                kundeOrig.geburtsdatum(),
                kundeOrig.homepage(),
                kundeOrig.geschlecht(),
                kundeOrig.familienstand(),
                kundeOrig.interessen(),
                umsatz,
                adresse
            );

            // when
            final var statusCode = client
                .put()
                .uri(ID_PATH, id)
                .contentType(APPLICATION_JSON)
                .bodyValue(kunde)
                .retrieve()
                .toBodilessEntity()
                .map(ResponseEntity::getStatusCode)
                .block();

            // then
            assertThat(statusCode).isEqualTo(NO_CONTENT);
        }

        @ParameterizedTest(name = "[{index}] Aendern eines vorhandenen Kunden durch PATCH: id={0}")
        @CsvSource(ID_UPDATE_PATCH + "," + NEUE_EMAIL_PATCH)
        @DisplayName("Aendern eines vorhandenen Kunden durch PATCH")
        void patch(final String id, final String email) {
            // given
            final var replaceOp = new PatchOperation(REPLACE, "/email", email);
            final var addOp = new PatchOperation(ADD, "/interessen", NEUES_INTERESSE.toString());
            final var removeOp = new PatchOperation(REMOVE, "/interessen", ZU_LOESCHENDES_INTERESSE.toString());
            final var operations = List.of(replaceOp, addOp, removeOp);

            // when
            final var statusCode = client
                .patch()
                .uri(ID_PATH, id)
                .contentType(APPLICATION_JSON)
                .bodyValue(operations)
                .retrieve()
                .toBodilessEntity()
                .map(ResponseEntity::getStatusCode)
                .block();

            // then
            assertThat(statusCode).isEqualTo(NO_CONTENT);
        }
    }
    @Nested
    class Loeschen {
        @ParameterizedTest(name = "[{index}] Loeschen eines vorhandenen Kunden: id={0}")
        @ValueSource(strings = ID_DELETE)
        @DisplayName("Loeschen eines vorhandenen Kunden")
        void deleteById(final String id) {
            // when
            final var statusCode = client
                .delete()
                .uri(ID_PATH, id)
                .exchangeToMono(response -> Mono.just(response.statusCode()))
                .block();

            // then
            assertThat(statusCode).isEqualTo(NO_CONTENT);
        }
    }
}
