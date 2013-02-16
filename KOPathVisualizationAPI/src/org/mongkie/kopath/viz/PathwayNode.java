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
package org.mongkie.kopath.viz;

import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.mongkie.kopath.Pathway;
import org.mongkie.kopath.viz.worker.RetrievalWorker;
import org.mongkie.visualization.MongkieDisplay;
import org.mongkie.visualization.VisualizationController;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class PathwayNode extends AbstractNode {

    private final Pathway p;
    private final RetrievalWorkerAction openAction;

    public PathwayNode(Pathway p) {
        super(Children.LEAF, Lookups.fixed(new Object[]{p}));
        this.p = p;
        openAction = new RetrievalWorkerAction(p);
        setDisplayName(p.getName());
        setIconBaseWithExtension("org/mongkie/kopath/viz/resources/pathway.png");
    }

    public Pathway getPathway() {
        return p;
    }

    @Override
    public Transferable drag() throws IOException {
        return p;
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[]{openAction};
    }

    @Override
    public Action getPreferredAction() {
        return openAction;
    }

    private static class RetrievalWorkerAction extends AbstractAction {

        private Pathway pathway;

        RetrievalWorkerAction(Pathway pathway) {
            super("Open...");
            this.pathway = pathway;
        }

        @Override
        public void actionPerformed(ActionEvent ev) {
            MongkieDisplay d = Lookup.getDefault().lookup(VisualizationController.class).getDisplay();
            if (d != null && d instanceof PathwayDisplay) {
                new RetrievalWorker((PathwayDisplay) d, pathway.getDatabase().getCode(), pathway.getId()).execute();
            } else {
                //TODO: open new display?
                throw new AssertionError();
            }
        }
    }
}
