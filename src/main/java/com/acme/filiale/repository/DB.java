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
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Emulation der Datenbasis für persistente Filialen.
 */
@Service
@SuppressWarnings({"UtilityClassCanBeEnum", "UtilityClass", "MagicNumber", "RedundantSuppression"})
public class DB {
    public final FilialenDBRepository filialenDBRepository;
    /**
     * Liste der Filialen zur Emulation der DB.
     */
    @SuppressWarnings("StaticCollection")
    public final List<Filiale> FILIALEN = getFilialen();

    public DB(FilialenDBRepository filialenDBRepository) {
        this.filialenDBRepository = filialenDBRepository;
    }

    @SuppressWarnings({"FeatureEnvy", "TrailingComment"})
    private List<Filiale> getFilialen() {
        if (this.filialenDBRepository != null) {
            return this.filialenDBRepository.findAll();
        }
        throw new RuntimeException();
    }
}
