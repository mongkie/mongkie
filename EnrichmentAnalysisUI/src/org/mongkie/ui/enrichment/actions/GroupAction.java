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
import org.mongkie.visualization.VisualizationController;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import prefuse.Visualization;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
@ActionID(category = "EnrichmentAnalysis",
id = "org.mongkie.ui.enrichment.actions.GroupAction")
@ActionRegistration(displayName = "#CTL_GroupAction", lazy = false)
@ActionReference(path = "EnrichmentAnalysis/Result/Actions", position = 1)
@Messages("CTL_GroupAction=Group")
public final class GroupAction extends MultipleTermAction {

    private static final int GROUP_MAX = 10;

    public GroupAction() {
        this(GlobalLookup.getDefault());
    }

    public GroupAction(Lookup lookup) {
        super(lookup);
        putValue(SMALL_ICON,
                ImageUtilities.loadImageIcon("org/mongkie/ui/enrichment/resources/group.png", false));
        putValue(NAME, NbBundle.getMessage(GroupAction.class, "CTL_GroupAction"));
        putValue(SHORT_DESCRIPTION, NbBundle.getMessage(GroupAction.class, "CTL_GroupAction"));
    }

    @Override
    protected boolean isEnabled(Collection<? extends EnrichedTerm> contexts) {
        for (EnrichedTerm term : contexts) {
            if (!Lookup.getDefault().lookup(EnrichmentController.class).isGrouped(term)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void performAction(Collection<? extends EnrichedTerm> contexts) {
        int i = 0;
        for (EnrichedTerm term : contexts) {
            if (++i > GROUP_MAX) {
                break;
            }
            Lookup.getDefault().lookup(EnrichmentController.class).group(term);
        }
        Lookup.getDefault().lookup(VisualizationController.class).getVisualization().rerun(Visualization.DRAW);
    }

    @Override
    public Action createContextAwareInstance(Lookup lookup) {
        return new GroupAction(lookup);
    }
}
