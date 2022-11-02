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
package com.acme.kunde.rest;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import static java.lang.Character.getNumericValue;

/**
 * Hilfsklasse um URIs für HATEOAS oder für URIs in ProblemDetail zu ermitteln, falls ein API-Gateway verwendet wird.
 *
 * @author <a href="mailto:Juergen.Zimmermann@h-ka.de">Jürgen Zimmermann</a>
 */
@Slf4j
@SuppressWarnings({"UtilityClassCanBeEnum", "UtilityClass"})
final class UriHelper {
    private static final String X_FORWARDED_PROTO = "x-forwarded-proto";

    private UriHelper() {
    }

    /**
     * Basis-URI ermitteln, d.h. ohne Query-Parameter.
     *
     * @param request Servlet-Request
     * @return Die Basis-URI als String
     */
    static String getBaseUri(final HttpServletRequest request) {
        return getBaseUri(request, null);
    }

    /**
     * Basis-URI ermitteln, d.h. ohne angehängten Pfad-Parameter für die ID und ohne Query-Parameter.
     *
     * @param request Servlet-Request
     * @param id Eine Kunde-ID oder null als Defaultwert
     * @return Die Basis-URI als String
     */
    @SuppressWarnings({"ReturnCount", "NPathComplexity", "CyclomaticComplexity", "ExecutableStatementCount"})
    static String getBaseUri(final HttpServletRequest request, final UUID id) {
        final var envoyOriginalPath = request.getHeader("x-envoy-original-path");
        if (envoyOriginalPath != null) {
            // Forwarding durch Envoy-Proxy, z.B. bei Istio
            return getBaseUriEnvoy(request, envoyOriginalPath);
        }

        final var forwardedHost = request.getHeader("x-forwarded-host");
        if (forwardedHost != null) {
            // Forwarding durch Ingress Controller oder Spring Cloud Gateway
            return getBaseUriForwarded(request, forwardedHost);
        }

        // KEIN Forwarding von einem API-Gateway
        var baseUri = request.getRequestURL().toString();
        final var indexQuestionMark = baseUri.indexOf(getNumericValue('?'));
        if (indexQuestionMark != -1) {
            baseUri = baseUri.substring(0, indexQuestionMark);
        }
        if (!baseUri.isEmpty() && baseUri.charAt(baseUri.length() - 1) == '/') {
            baseUri = baseUri.substring(0, baseUri.length() - 1);
        }
        if (id == null) {
            return baseUri;
        }
        final var idStr = id.toString();
        if (baseUri.endsWith("/" + idStr)) {
            baseUri = baseUri.substring(0, baseUri.length() - idStr.length() - 1);
        }
        return baseUri;
    }

    private static String getBaseUriEnvoy(final HttpServletRequest request, final String envoyOriginalPath) {
        // host: "localhost"
        // x-forwarded-proto: "http"
        // x-envoy-decorator-operation: "kunde.acme.svc.cluster.local:8080/kunden/*",
        // x-envoy-original-path: "/kunden/api/00000000-0000-0000-0000-000000000001"
        final var host = request.getHeader("Host");
        if (host == null) {
            throw new IllegalStateException("Kein \"Host\" im Header");
        }
        final var forwardedProto = request.getHeader(X_FORWARDED_PROTO);
        if (forwardedProto == null) {
            throw new IllegalStateException("Kein \"" + X_FORWARDED_PROTO + "\" im Header");
        }
        var basePath = envoyOriginalPath;
        final var indexQuestionMark = basePath.indexOf(getNumericValue('?'));
        if (indexQuestionMark != -1) {
            basePath = basePath.substring(0, indexQuestionMark);
        }
        if (!basePath.isEmpty() && basePath.charAt(basePath.length() - 1) == '/') {
            basePath = basePath.substring(0, basePath.length() - 1);
        }
        return forwardedProto + "://" + host + basePath;
    }

    private static String getBaseUriForwarded(final HttpServletRequest request, final String forwardedHost) {
        // x-forwarded-proto: "https"
        // x-forwarded-host: "kubernetes.docker.internal"
        // x-forwarded-prefix: null bei Ingress Controller bzw. "/kunden" bei Spring Cloud Gateway
        final var forwardedProto = request.getHeader(X_FORWARDED_PROTO);
        if (forwardedProto == null) {
            throw new IllegalStateException("Kein \"" + X_FORWARDED_PROTO + "\" im Header");
        }

        var forwardedPrefix = request.getHeader("x-forwarded-prefix");
        if (forwardedPrefix == null) {
            forwardedPrefix = "";
        }
        final var baseUri = forwardedProto + "://" + forwardedHost + forwardedPrefix;
        log.debug("baseUri = {}", baseUri);
        return baseUri;
    }

    /**
     * Original-URI ermitteln, falls Istio bzw. Envoy genutzt wird.
     *
     * @param request Servlet-Request
     * @return Original-URI
     */
    @SneakyThrows(URISyntaxException.class)
    static URI getRequestUri(final HttpServletRequest request) {
        final var envoyOriginalPath = request.getHeader("x-envoy-original-path");
        if (envoyOriginalPath == null) {
            return new URI(request.getRequestURL().toString());
        }
        final var host = request.getHeader("Host");
        final var forwardedProto = request.getHeader(X_FORWARDED_PROTO);
        return URI.create(forwardedProto + "://" + host + envoyOriginalPath);
    }
}
