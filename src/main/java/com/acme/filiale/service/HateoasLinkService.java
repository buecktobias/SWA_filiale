package com.acme.filiale.service;

import com.acme.filiale.entity.Adresse;
import com.acme.filiale.entity.Filiale;
import com.acme.filiale.entity.Umsatz;
import com.acme.filiale.rest.FilialeDTO;
import com.acme.filiale.rest.FilialeGetController;
import com.acme.filiale.rest.FilialeWriteController;
import com.acme.filiale.rest.FilialenModel;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Currency;
import java.util.HashMap;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * HATEOAS Link Helper Klasse.
 */
@Service
public class HateoasLinkService {
    /**
     * Erstellt ein Filialenmodell.
     *
     * @param filiale Filiale.
     * @param baseUri BaseUri.
     * @return ein Filialenmodel.
     */
    public FilialenModel getFilialenModelFromFiliale(final Filiale filiale, final String baseUri) {

        final var model = new FilialenModel(filiale);
        final var idUri = baseUri + "/" + filiale.getId();
        final var id = filiale.getId();
        final var selfLink = Link.of(idUri);
        final var listLink = linkTo(methodOn(FilialeGetController.class).find(new LinkedMultiValueMap<>(), null)).withRel("list");
        final Link addLink;
        final var umsatz = Umsatz.builder()
            .betrag(new BigDecimal("0.0"))
                .waehrung(Currency.getInstance("EUR")).build();
        final var adresse = Adresse.builder().plz("55122").ort("Mainz").build();
        final URL homepage;
        try {
            homepage = new URL("http://localhost:8080");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        final var filialeDTO = new FilialeDTO("", "", homepage, umsatz, adresse);
        try {
            addLink = linkTo(methodOn(FilialeWriteController.class).create(filialeDTO, null)).withRel("add");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        final var updateLink = linkTo(methodOn(FilialeWriteController.class).update(id, filialeDTO)).withRel("update");
        model.add(selfLink, listLink, addLink, updateLink);
        return model;
    }
}
