/*
 *  This file is part of MONGKIE. Visit <http://www.mongkie.org/> for details.
 *  Copyright (C) 2012 Korean Bioinformation Center(KOBIC)
 * 
 *  MONGKIE is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  MONGKE is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 * 
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.mongkie.ui.visualization.options;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.mongkie.ui.visualization.options.spi.Options;
import org.mongkie.visualization.MongkieDisplay;
import org.openide.util.lookup.ServiceProvider;
import prefuse.Visualization;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.util.Index;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
//@ServiceProvider(service = Options.class, position = 1000)
public class SomeActions implements Options {

    @Override
    public String getName() {
        return "Some Actions";
    }

    @Override
    public List<JComponent> createTools(final MongkieDisplay display) {
        List<JComponent> tools = new ArrayList<JComponent>();
        JButton button = new JButton(new AbstractAction("DO") {

            @Override
            public void actionPerformed(ActionEvent e) {
                display.getVisualization().process(new Runnable() {

                    @Override
                    public void run() {
                        Graph g = display.getGraph();
                        Index nodeIdx = g.getNodeTable().index("node");
                        int row = nodeIdx.get("hsa-let-7a");
                        Node nodea = row < 0 ? g.addNode() : g.getNode(row);
                        if (row < 0) {
                            nodea.setString("node", "hsa-let-7a");
                        }
                        row = nodeIdx.get("hsa-let-7b");
                        Node nodeb = row < 0 ? g.addNode() : g.getNode(row);
                        if (row < 0) {
                            nodeb.setString("node", "hsa-let-7b");
                        }
                        row = nodeIdx.get("EPHB2");
                        Node nodea1 = row < 0 ? g.addNode() : g.getNode(row);
                        if (row < 0) {
                            nodea1.setString("node", "EPHB2");
                        }
                        row = nodeIdx.get("HOXA10");
                        Node nodea2 = row < 0 ? g.addNode() : g.getNode(row);
                        if (row < 0) {
                            nodea2.setString("node", "HOXA10");
                        }
                        row = nodeIdx.get("NCAPG");
                        Node nodea3 = row < 0 ? g.addNode() : g.getNode(row);
                        if (row < 0) {
                            nodea3.setString("node", "NCAPG");
                        }
                        row = nodeIdx.get("RET");
                        Node nodea4 = row < 0 ? g.addNode() : g.getNode(row);
                        if (row < 0) {
                            nodea4.setString("node", "RET");
                        }
                        row = nodeIdx.get("RET");
                        Node nodeb1 = row < 0 ? g.addNode() : g.getNode(row);
                        if (row < 0) {
                            nodeb1.setString("node", "RET");
                        }
                        g.addEdge(nodea, nodea1);
                        g.addEdge(nodea, nodea2);
                        g.addEdge(nodea, nodea3);
                        g.addEdge(nodea, nodea4);
                        g.addEdge(nodeb, nodeb1);
                    }
                }, Visualization.DRAW);
            }
        });
        tools.add(button);
        return tools;
    }

    @Override
    public JPanel createSettingPanel(MongkieDisplay display) {
        return null;
    }

    @Override
    public boolean hasTools() {
        return true;
    }

    @Override
    public boolean hasSettingPanel() {
        return false;
    }
}
