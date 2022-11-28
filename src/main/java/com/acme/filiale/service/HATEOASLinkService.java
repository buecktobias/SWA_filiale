package com.acme.filiale.service;

import com.acme.filiale.entity.Filiale;
import com.acme.filiale.rest.FilialeGetController;
import com.acme.filiale.rest.FilialeWriteController;
import com.acme.filiale.rest.FilialenModel;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Service;

import java.net.URISyntaxException;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Service
public class HATEOASLinkService {
    public FilialenModel getFilialenModelFromFiliale(Filiale filiale, String baseUri){
        final var model = new FilialenModel(filiale);
        final var idUri = baseUri + "/" + filiale.getId();
        final var id = filiale.getId();
        final var selfLink = Link.of(idUri);
        final var listLink = linkTo(methodOn(FilialeGetController.class).find(null, null)).withRel("list");
        final Link addLink;
        try {
            addLink = linkTo(methodOn(FilialeWriteController.class).create(null, null)).withRel("add");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        final var updateLink = linkTo(methodOn(FilialeWriteController.class).update(id, null)).withRel("update");
        model.add(selfLink, listLink, addLink, updateLink);
        return model;
    }
}
