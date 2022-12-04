package com.acme.filiale.rest;

import com.acme.filiale.entity.Adresse;
import com.acme.filiale.entity.Filiale;
import com.acme.filiale.entity.Umsatz;
import java.net.URL;

/**
 * ValueObject für das Neuanlegen und Ändern einer neuen Filiale. Beim Lesen wird die Klasse FilialeModel
 * für die Ausgabe verwendet.
 *
 * @param name Gültiger Name eines filialen, d.h. mit einem geeigneten Muster.
 * @param email Email der Filiale.
 * @param homepage Die Homepage der Filiale.
 * @param umsatz Der Umsatz der Filiale.
 * @param adresse Die Adresse der Filiale.
 */
@SuppressWarnings("RecordComponentNumber")
public record FilialeDTO(
    String name,
    String email,
    URL homepage,
    Umsatz umsatz,
    Adresse adresse
) {
    /**
     * Konvertierung in ein Objekt des Anwendungskerns.
     *
     * @return filialeobjekt für den Anwendungskern
     */
    Filiale toFiliale() {
        return Filiale
            .builder()
            .id(null)
            .name(name)
            .email(email)
            .homepage(homepage)
            .umsatz(umsatz)
            .adresse(adresse)
            .build();
    }
}
