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
package org.mongkie.ui.enrichment.actions;

import java.util.Collection;
import javax.swing.Action;
import org.mongkie.enrichment.EnrichmentController;
import org.mongkie.enrichment.spi.EnrichedTerm;
import org.mongkie.enrichment.util.MultipleTermAction;
import org.mongkie.util.GlobalLookup;
import org.mongkie.visualization.VisualizationControllerUI;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import prefuse.data.Graph;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
@ActionID(category = "EnrichmentAnalysis",
id = "org.mongkie.ui.enrichment.actions.NewGraphAction")
@ActionRegistration(displayName = "#CTL_NewGraphAction", lazy = false)
@ActionReference(path = "EnrichmentAnalysis/Result/Actions", position = 0)
@Messages("CTL_NewGraphAction=New Graph")
public final class NewGraphAction extends MultipleTermAction {

    private static final int NEW_GRAPH_MAX = 10;

    public NewGraphAction() {
        this(GlobalLookup.getDefault());
    }

    public NewGraphAction(Lookup lookup) {
        super(lookup);
        putValue(SMALL_ICON,
                ImageUtilities.loadImageIcon("org/mongkie/ui/enrichment/resources/new.png", false));
        putValue(NAME, NbBundle.getMessage(NewGraphAction.class, "CTL_NewGraphAction"));
        putValue(SHORT_DESCRIPTION, NbBundle.getMessage(NewGraphAction.class, "CTL_NewGraphAction"));
    }

    @Override
    protected boolean isEnabled(Collection<? extends EnrichedTerm> contexts) {
        return true;
    }

    @Override
    protected void performAction(Collection<? extends EnrichedTerm> contexts) {
        int i = 0;
        EnrichmentController controller = Lookup.getDefault().lookup(EnrichmentController.class);
        Graph g = controller.getModel().getDisplay().getGraph();
        for (EnrichedTerm term : contexts) {
            if (++i > NEW_GRAPH_MAX) {
                break;
            }
            Lookup.getDefault().lookup(VisualizationControllerUI.class).openNewDisplayTopComponent(
                    term.getName(),
                    g.createPartial(controller.findNodesInDisplayBelongTo(term)));
        }
    }

    @Override
    public Action createContextAwareInstance(Lookup lookup) {
        return new NewGraphAction(lookup);
    }
}
