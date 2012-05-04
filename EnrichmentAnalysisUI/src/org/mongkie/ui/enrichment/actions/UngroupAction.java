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
import org.mongkie.util.GlobalLookup;
import org.mongkie.util.MultipleContextAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
@ActionID(category = "EnrichmentAnalysis",
id = "org.mongkie.ui.enrichment.actions.UngroupAction")
@ActionRegistration(displayName = "#CTL_UngroupAction",
iconBase = "org/mongkie/ui/enrichment/resources/ungroup.png")
@ActionReference(path = "EnrichmentAnalysis/Result/Actions", position = 2)
@Messages("CTL_UngroupAction=Ungroup")
public final class UngroupAction extends MultipleContextAction<EnrichedTerm> {

    public UngroupAction() {
        this(GlobalLookup.getDefault());
    }

    public UngroupAction(Lookup lookup) {
        super(EnrichedTerm.class, lookup);
        putValue(SMALL_ICON,
                ImageUtilities.loadImageIcon("org/mongkie/ui/enrichment/resources/ungroup.png", false));
        putValue(NAME, NbBundle.getMessage(GroupAction.class, "CTL_UngroupAction"));
        putValue(SHORT_DESCRIPTION, NbBundle.getMessage(UngroupAction.class, "CTL_UngroupAction"));
    }

    @Override
    protected boolean isEnabled(Collection<? extends EnrichedTerm> contexts) {
        for (EnrichedTerm term : contexts) {
            if (Lookup.getDefault().lookup(EnrichmentController.class).isGrouped(term)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void performAction(Collection<? extends EnrichedTerm> contexts) {
        for (EnrichedTerm term : contexts) {
            Lookup.getDefault().lookup(EnrichmentController.class).ungroup(term);
        }
    }

    @Override
    public Action createContextAwareInstance(Lookup lookup) {
        return new UngroupAction(lookup);
    }
}
