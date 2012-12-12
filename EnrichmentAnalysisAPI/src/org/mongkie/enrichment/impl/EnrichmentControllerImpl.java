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
package org.mongkie.enrichment.impl;

import java.awt.Color;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.mongkie.enrichment.EnrichmentController;
import org.mongkie.enrichment.EnrichmentResultUIProvider;
import org.mongkie.enrichment.spi.EnrichedTerm;
import org.mongkie.enrichment.spi.Enrichment;
import org.mongkie.longtask.progress.Progress;
import org.mongkie.longtask.progress.ProgressTask;
import org.mongkie.longtask.progress.ProgressTicket;
import org.mongkie.visualization.MongkieDisplay;
import org.mongkie.visualization.group.GroupingSupport;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Tuple;
import prefuse.data.expression.AbstractPredicate;
import prefuse.visual.AggregateItem;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
@ServiceProvider(service = EnrichmentController.class)
public class EnrichmentControllerImpl extends EnrichmentController<EnrichmentModelImpl> {

    private GroupingSupport<EnrichedTerm> gs = new GroupingSupport<EnrichedTerm>(this);

    @Override
    public void analyze(String geneIdColumn, final String... genes) {
        final Enrichment en = model.get();
        model.setGeneIdColumn(geneIdColumn);
        final EnrichmentTask task = new EnrichmentTask(en);
        model.getExecutor().execute(task, new Runnable() {
            @Override
            public void run() {
                ProgressTicket ticket = task.getProgressTicket();
                try {
                    Progress.setDisplayName(ticket, en.getBuilder().getName() + " Enrichment Analysis");
                    Progress.start(ticket);
                    model.setResult(en, en.execute(genes));
                } finally {
                    Progress.finish(ticket);
                }
            }
        });
    }

    @Override
    public void cancelAnalyzing() {
        model.getExecutor().cancel();
    }

    @Override
    protected Class<EnrichmentModelImpl> getModelClass() {
        return EnrichmentModelImpl.class;
    }

    @Override
    protected EnrichmentModelImpl createModel(MongkieDisplay d) {
        return new EnrichmentModelImpl(d);
    }

    @Override
    public Set<Node> findNodesInDisplayBelongTo(EnrichedTerm term) {
        if (termToNodes.containsKey(term)) {
            return termToNodes.get(term);
        }

        Set<Node> nodes = new HashSet<Node>();
        final List<String> enrichedGeneIds = Arrays.asList(term.getEnrichedGeneIds());
        final String geneIdColumn = model.getGeneIdColumn();
        Graph g = model.getDisplay().getGraph();
        for (Iterator<Tuple> nodeIter = g.getNodeTable().tuples(new AbstractPredicate() {
            @Override
            public boolean getBoolean(Tuple t) {
                String ids = t.getString(geneIdColumn);
                if (ids != null) {
                    for (String id : ids.split(",")) {
                        if (enrichedGeneIds.contains(id.trim())) {
                            return true;
                        }
                    }
                }
                return false;
            }
        }); nodeIter.hasNext();) {
            nodes.add(g.getNode(nodeIter.next().getRow()));
        }

        termToNodes.put(term, nodes);
        return nodes;
    }
    private final Map<EnrichedTerm, Set<Node>> termToNodes = new HashMap<EnrichedTerm, Set<Node>>();

    @Override
    public void clearResult() {
        Enrichment en = model.get();
        for (Iterator<EnrichedTerm> termIter = termToNodes.keySet().iterator(); termIter.hasNext();) {
            EnrichedTerm term = termIter.next();
            ungroup(term);
            if (en.getBuilder().isFor(term)) {
                termIter.remove();
            }
        }
        model.clearResult(en);
    }

    @Override
    public AggregateItem group(EnrichedTerm term) {
        return gs.group(term);
    }

    @Override
    public void ungroup(EnrichedTerm term) {
        gs.ungroup(term);
    }

    @Override
    public boolean isGrouped(EnrichedTerm term) {
        return gs.isGrouped(term);
    }

    @Override
    public AggregateItem getGroup(EnrichedTerm term) {
        return gs.getGroup(term);
    }

    @Override
    public Collection<Node> getNodes(EnrichedTerm term) {
        return findNodesInDisplayBelongTo(term);
    }

    @Override
    public String getName(EnrichedTerm term) {
        return term.getName();
    }

    @Override
    public Color getColor(EnrichedTerm term) {
        return term.getColor();
    }

    @Override
    public void ungrouped(AggregateItem group) {
        Lookup.getDefault().lookup(EnrichmentResultUIProvider.class).getUI().touchLookupContents();
    }

    private static class EnrichmentTask extends ProgressTask {

        private final Enrichment en;

        public EnrichmentTask(Enrichment en) {
            this.en = en;
        }

        @Override
        public boolean cancel() {
            return en.cancel();
        }
    }
}
