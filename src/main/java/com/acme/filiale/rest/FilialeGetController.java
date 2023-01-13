package com.acme.filiale.rest;

import com.acme.filiale.service.FilialeReadService;
import com.acme.filiale.service.HateoasLinkService;
import com.acme.filiale.service.NotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.Objects;


import static com.acme.filiale.rest.UriHelper.getBaseUri;
import static org.springframework.hateoas.MediaTypes.HAL_JSON_VALUE;
import static org.springframework.http.ResponseEntity.notFound;

/**
 * Eine @RestController-Klasse bildet die REST-Schnittstelle, wobei die HTTP-Methoden, Pfade und MIME-Typen auf die
 * Methoden der Klasse abgebildet werden.
 * <img src="../../../../../extras/doc/FilialeGetControllerUML-0.png" alt="Klassendiagramm">
 *
 */
@RestController
@RequestMapping("/")
@RequiredArgsConstructor
@Slf4j
public class FilialeGetController {
    /**
     * Pfad, um Namen abzufragen.
     */
    @SuppressWarnings("TrailingComment")
    static final String NAMEN_PATH = "/name";

    private final FilialeReadService service;
    private final HateoasLinkService linkService;


    /**
     * Suche anhand der filiale-ID als Pfad-Parameter.
     *
     * @param id      ID des zu suchenden filialen
     * @param request Das Request-Objekt, um Links für HATEOAS zu erstellen.
     * @return Ein Response mit dem Statuscode 200 und dem gefundenen filialen mit Atom-Links oder Statuscode 404.
     */
    @GetMapping(path = "{id}", produces = HAL_JSON_VALUE)
    @Operation(summary = "Suche mit der Filialen-ID", tags = "Suchen")
    @ApiResponse(responseCode = "200", description = "filiale gefunden")
    @ApiResponse(responseCode = "404", description = "filiale nicht gefunden")
    FilialenModel findById(@PathVariable final Long id, final HttpServletRequest request) {
        log.debug("findById: id={}", id);

        final var filiale = service.findById(id);
        log.debug("findById: {}", filiale);

        final var baseUri = getBaseUri(request, id);

        return linkService.getFilialenModelFromFiliale(filiale, baseUri);
    }

    /**
     * Suche mit diversen Suchkriterien als Query-Parameter.
     *
     * @param suchkriterien Query-Parameter als Map.
     * @param request       Das Request-Objekt, um Links für HATEOAS zu erstellen.
     * @return Ein Response mit dem Statuscode 200 und den gefundenen filialen als CollectionModel oder Statuscode 404.
     */
    @GetMapping(produces = HAL_JSON_VALUE)
    @Operation(summary = "Suche mit Suchkriterien", tags = "Suchen")
    @ApiResponse(responseCode = "200", description = "CollectionModel mid den Kunden")
    @ApiResponse(responseCode = "404", description = "Keine FIlialen gefunden")
    public CollectionModel<? extends FilialenModel> find(
        final @RequestParam MultiValueMap<String, String> suchkriterien,
        final HttpServletRequest request
    ) {
        final MultiValueMap<String, String> suchkriterienMap = Objects.requireNonNullElseGet(suchkriterien, LinkedMultiValueMap::new);
        log.debug("find: suchkriterien={}", suchkriterienMap);

        final var baseUri = getBaseUri(request);

        final var models = service.find(suchkriterienMap)
            .stream()
            .map(kunde -> linkService.getFilialenModelFromFiliale(kunde, baseUri))
            .toList();

        log.debug("find: {}", models);
        return CollectionModel.of(models);
    }

    /**
     * Abfrage, welche Name es zu einem Präfix gibt.
     *
     * @param prefix Namen-Präfix als Pfadvariable.
     * @return Die passenden Namen oder Statuscode 404, falls es keine gibt.
     */
    @GetMapping(path = NAMEN_PATH + "/{prefix}", produces = HAL_JSON_VALUE)
    String findNameByPrefix(@PathVariable final String prefix) {
        log.debug("findNameByPrefix: {}", prefix);
        final var name = service.findNameByPrefix(prefix);
        log.debug("findNameByPrefix: {}", name);
        return name.toString();
    }

    @ExceptionHandler(NotFoundException.class)
    @SuppressWarnings("unused")
    ResponseEntity<Void> handleNotFound(final NotFoundException ex) {
        log.debug("handleNotFound: {}", ex.getMessage());
        return notFound().build();
    }
}
