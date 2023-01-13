package com.acme.filiale.service;

import com.acme.filiale.entity.Filiale;
import com.acme.filiale.repository.FilialenDBRepository;
import com.acme.filiale.repository.FilialenRepository;
import com.acme.filiale.repository.SpecBuilder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * FialialeReadService liest Filialen.
 * <img src="../../../../../extras/doc/FilialeReadServiceUML-0.png" alt="Klassendiagramm">
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FilialeReadService {
    private final FilialenDBRepository repo;
    private final SpecBuilder specBuilder;

    /**
     * Eine Filiale anhand seiner ID suchen.
     *
     * @param id Die Id der gesuchten Filiale
     * @return Der gefundene Kunde
     * @throws NotFoundException Falls kein Kunde gefunden wurde
     */
    public @NonNull Filiale findById(final Long id) {
        log.debug("findById: id={}", id);
        final var filiale = repo.findById(id)
            .orElseThrow(() -> new NotFoundException(id));
        log.debug("findById: {}", filiale);
        return filiale;
    }

    /**
     * Filiale anhand von Suchkriterien als Collection suchen.
     *
     * @param suchkriterien Die Suchkriterien
     * @return Die gefundenen Filiale oder eine leere Liste
     * @throws NotFoundException Falls keine Filiale gefunden wurden
     */
    @SuppressWarnings({"ReturnCount", "NestedIfDepth"})
    public Collection<Filiale> find(final Map<String, List<String>> suchkriterien) {
        log.debug("find: suchkriterien={}", suchkriterien);

        if (suchkriterien == null || suchkriterien.isEmpty()) {
            return repo.findAll();
        }
        final var specs = specBuilder.build(suchkriterien)
            .orElseThrow(() -> new NotFoundException(suchkriterien));
        final var filialen = repo.findAll(specs);
        if (filialen.isEmpty()) {
            throw new NotFoundException(suchkriterien);
        }
        log.debug("find: {}", filialen);
        return filialen;
    }

    /**
     * Abfrage, welche Namen es zu einem Präfix gibt.
     *
     * @param prefix Namen-Präfix.
     * @return Die passenden Name.
     * @throws NotFoundException Falls kein Name gefunden wurden.
     */
    public Collection<String> findNameByPrefix(final String prefix) {
        final var name = repo.findNameByPrefix(prefix);
        if (name.isEmpty()) {
            throw new NotFoundException();
        }
        return name;
    }
}
