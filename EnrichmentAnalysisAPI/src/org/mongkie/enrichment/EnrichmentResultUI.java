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
package org.mongkie.enrichment;

import javax.swing.Action;
import org.mongkie.enrichment.spi.EnrichedResultUI;
import org.mongkie.enrichment.spi.EnrichedTerm;
import org.mongkie.enrichment.spi.EnrichmentResultUISupport;
import org.openide.util.Lookup;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public interface EnrichmentResultUI extends Lookup.Provider {

    public boolean isValidResult();

    public EnrichmentResultUISupport getResultUISupport();

    public boolean isBusy();

    public EnrichedResultUI getResult();

    public void setLookupContents(EnrichedTerm... terms);

    public void touchLookupContents();

    public Action[] getContextActions();
}
