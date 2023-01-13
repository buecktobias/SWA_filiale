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
package com.acme.filiale.repository;

import com.acme.filiale.entity.Filiale;
import com.acme.filiale.entity.Filiale.*;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Singleton-Klasse, um Specifications für Queries in Spring Data zu bauen.
 *
 * @author <a href="mailto:Juergen.Zimmermann@h-ka.de">Jürgen Zimmermann</a>
 */
@Component
@Slf4j
public class SpecBuilder {
    /**
     * Specification für eine Query mit Spring Data bauen.
     *
     * @param queryParams als MultiValueMap
     * @return Specification für eine Query mit Spring Data
     */
    public Optional<Specification<Filiale>> build(final Map<String, ? extends List<String>> queryParams) {
        log.debug("build: queryParams={}", queryParams);

        if (queryParams.isEmpty()) {
            // keine Suchkriterien
            return Optional.empty();
        }

        List<Specification<Filiale>> specs = queryParams
            .entrySet()
            .stream()
            .map(entry -> toSpec(entry.getKey(), entry.getValue()))
            .toList();

        if (specs.isEmpty() || specs.contains(null)) {
            return Optional.empty();
        }


        return Optional.of(Specification.allOf(specs));
    }

    @SuppressWarnings("CyclomaticComplexity")
    private Specification<Filiale> toSpec(final String paramName, final List<String> paramValues) {
        log.trace("toSpec: paramName={}, paramValues={}", paramName, paramValues);

        if (paramValues == null || paramValues.size() != 1) {
            return null;
        }

        final var value = paramValues.get(0);
        return switch (paramName) {
            case "name" -> name(value);
            case "email" ->  email(value);
            case "plz" -> plz(value);
            case "ort" -> ort(value);
            default -> null;
        };
    }


    private Specification<Filiale> name(final String teil) {
        // root ist jakarta.persistence.criteria.Root<Filiale>
        // query ist jakarta.persistence.criteria.CriteriaQuery<Filiale>
        // builder ist jakarta.persistence.criteria.CriteriaBuilder
        // https://www.logicbig.com/tutorials/java-ee-tutorial/jpa/meta-model.html
        return new Specification<Filiale>() {
            @Override
            public Predicate toPredicate(Root<Filiale> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                root.fetch("name");
                return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("name")),
                    criteriaBuilder.lower(criteriaBuilder.literal("%" + teil+ "%"))
                );
            }
        };
    }

    private Specification<Filiale> email(final String teil) {
        return new Specification<Filiale>() {
            @Override
            public Predicate toPredicate(Root<Filiale> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("email")),
                    criteriaBuilder.lower(criteriaBuilder.literal("%" + teil+ "%"))
                );
            }
        };
    }


    private Specification<Filiale> plz(final String prefix) {
        return new Specification<Filiale>() {
            @Override
            public Predicate toPredicate(Root<Filiale> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                root.fetch("adresse");
                return criteriaBuilder.like(
                    root.get("adresse").get("plz"),
                    prefix + '%');
            }
        };
    }

    private Specification<Filiale> ort(final String prefix) {
        return new Specification<Filiale>() {
            @Override
            public Predicate toPredicate(Root<Filiale> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                root.fetch("adresse");
                return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("adresse").get("ort")),
                    criteriaBuilder.lower(criteriaBuilder.literal(prefix +'%'))
                );
            }
        };
    }
}
