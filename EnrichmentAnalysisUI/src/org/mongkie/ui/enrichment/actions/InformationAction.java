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

import javax.swing.Action;
import org.mongkie.enrichment.EnrichmentResultUIProvider;
import org.mongkie.enrichment.spi.EnrichedTerm;
import org.mongkie.enrichment.spi.EnrichmentResultUISupport;
import org.mongkie.util.GlobalLookup;
import org.mongkie.util.SingleContextAction;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
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
id = "org.mongkie.ui.enrichment.actions.InformationAction")
@ActionRegistration(displayName = "#CTL_InformationAction", lazy = false)
@ActionReference(path = "EnrichmentAnalysis/Result/Actions", position = 3)
@Messages("CTL_InformationAction=Term Information")
public final class InformationAction extends SingleContextAction<EnrichedTerm> {

    public InformationAction() {
        this(GlobalLookup.getDefault());
    }

    public InformationAction(Lookup lookup) {
        super(EnrichedTerm.class, lookup);
        putValue(SMALL_ICON,
                ImageUtilities.loadImageIcon("org/mongkie/ui/enrichment/resources/information.png", false));
        putValue(NAME, NbBundle.getMessage(InformationAction.class, "CTL_InformationAction"));
        putValue(SHORT_DESCRIPTION, NbBundle.getMessage(InformationAction.class, "CTL_InformationAction"));
    }

    @Override
    protected boolean isEnabled(EnrichedTerm term) {
        EnrichmentResultUISupport support =
                Lookup.getDefault().lookup(EnrichmentResultUIProvider.class).getUI().getResultUISupport();
        return support != null && support.getInformationPanel(term) != null;
    }

    @Override
    protected void performAction(EnrichedTerm term) {
        NotifyDescriptor reportDescriptor = new NotifyDescriptor.Message(
                Lookup.getDefault().lookup(EnrichmentResultUIProvider.class).getUI().getResultUISupport().getInformationPanel(term),
                NotifyDescriptor.INFORMATION_MESSAGE);
        reportDescriptor.setTitle("Term Information");
        DialogDisplayer.getDefault().notify(reportDescriptor);
    }

    @Override
    public Action createContextAwareInstance(Lookup lookup) {
        return new InformationAction(lookup);
    }
}
