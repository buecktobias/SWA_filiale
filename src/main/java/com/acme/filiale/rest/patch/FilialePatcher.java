package com.acme.filiale.rest.patch;

import com.acme.filiale.entity.Filiale;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.stream.Collectors;

import static com.acme.filiale.rest.patch.PatchOperationType.ADD;
import static com.acme.filiale.rest.patch.PatchOperationType.REMOVE;
import static com.acme.filiale.rest.patch.PatchOperationType.REPLACE;

/**
 * Klasse, um PATCH-Operationen auf filiale-Objekte anzuwenden.
 *
 * @author <a href="mailto:Juergen.Zimmermann@h-ka.de">Jürgen Zimmermann</a>
 */
@Component
@Slf4j
public final class FilialePatcher {
    FilialePatcher() {
    }

    /**
     * PATCH-Operationen werden auf ein filiale-Objekt angewandt.
     *
     * @param filiale    Das zu modifizierende filiale-Objekt.
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

        final var removeOps = operations.stream()
            .filter(op -> op.op() == REMOVE)
            .collect(Collectors.toList());
        log.debug("patch: removeOps={}", removeOps);
    }

    private void replaceOps(final Filiale filiale, final Iterable<PatchOperation> ops) {
        ops.forEach(op -> {
            switch (op.path()) {
                case "/name" -> filiale.setName(op.value());
                case "/email" -> filiale.setEmail(op.value());
                default -> throw new InvalidPatchOperationException();
            }
        });
        log.trace("replaceOps: filiale={}", filiale);
    }
}
