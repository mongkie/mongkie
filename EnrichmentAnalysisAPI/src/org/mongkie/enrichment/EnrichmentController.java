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

import java.util.Set;
import org.mongkie.enrichment.spi.EnrichedTerm;
import org.mongkie.enrichment.spi.Enrichment;
import org.mongkie.visualization.group.GroupingSupportable;
import org.mongkie.visualization.workspace.AbstractController;
import prefuse.data.Node;

/**
 * 
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public abstract class EnrichmentController<M extends EnrichmentModel>
        extends AbstractController<M, Enrichment> implements GroupingSupportable<EnrichedTerm> {

    public void setEnrichment(Enrichment e) {
        setModelData(e);
    }

    public abstract void analyze(String geneIdColumn, String... genes);

    public abstract void cancelAnalyzing();

    public abstract Set<Node> findNodesInDisplayBelongTo(EnrichedTerm term);

    public abstract void clearResult();
}
