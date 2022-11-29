package com.acme.filiale.service;

import com.acme.filiale.entity.Filiale;
import com.acme.filiale.repository.FilialenRepository;
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
public final class FilialeReadService {
    private final FilialenRepository repo;

    /**
     * Eine Filiale anhand seiner ID suchen.
     *
     * @param id Die Id der gesuchten Filiale
     * @return Der gefundene Kunde
     * @throws NotFoundException Falls kein Kunde gefunden wurde
     */
    public @NonNull Filiale findById(final UUID id) {
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
    public Collection<Filiale> find(final Map<String, String> suchkriterien) {
        log.debug("find: suchkriterien={}", suchkriterien);

        if (suchkriterien == null || suchkriterien.isEmpty()) {
            return repo.findAll();
        }

        if (suchkriterien.size() == 1) {
            final var name = suchkriterien.get("name");
            if (name != null) {
                final var filiale = repo.findByName(name);
                if (filiale.isEmpty()) {
                    throw new NotFoundException(suchkriterien);
                }
                log.debug("find (name): {}", filiale);
                return filiale;
            }

            final var email = suchkriterien.get("email");
            if (email != null) {
                final var kunde = repo.findByEmail(email);
                if (kunde.isEmpty()) {
                    throw new NotFoundException(suchkriterien);
                }
                final var filialen = List.of(kunde.get());
                log.debug("find (email): {}", filialen);
                return filialen;
            }
        }

        final var filialen = repo.find(suchkriterien);
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
        final var name = repo.findNamenByPrefix(prefix);
        if (name.isEmpty()) {
            throw new NotFoundException();
        }
        return name;
    }
}
