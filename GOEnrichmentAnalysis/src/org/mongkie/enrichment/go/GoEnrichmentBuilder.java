/*
 * This file is part of MONGKIE. Visit <http://www.mongkie.org/> for details.
 * Copyright (C) 2011 Korean Bioinformation Center(KOBIC)
 * 
 * MONGKIE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MONGKIE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.mongkie.enrichment.go;

import org.mongkie.enrichment.spi.EnrichedTerm;
import org.mongkie.enrichment.spi.Enrichment;
import org.mongkie.enrichment.spi.EnrichmentBuilder;
import org.mongkie.ui.enrichment.go.GoEnrichmentSettingUI;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
@ServiceProvider(service = EnrichmentBuilder.class)
public class GoEnrichmentBuilder implements EnrichmentBuilder {

    private final GoEnrichment go = new GoEnrichment(this);
    private final GoEnrichmentSettingUI settings = new GoEnrichmentSettingUI();

    @Override
    public Enrichment getEnrichment() {
        return go;
    }

    @Override
    public String getName() {
        return "Gene Ontology";
    }

    @Override
    public String getDescription() {
        return "N/A";
    }

    @Override
    public SettingUI getSettingUI() {
        return settings;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public boolean isFor(EnrichedTerm term) {
        return term instanceof EnrichedGoId;
    }
}
