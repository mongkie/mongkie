/*
 *  This file is part of MONGKIE. Visit <http://www.mongkie.org/> for details.
 *  Copyright (C) 2011 Korean Bioinformation Center(KOBIC)
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
package org.mongkie.ui.visualmap.partition;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Enumeration;
import javax.swing.*;
import javax.swing.border.Border;
import org.mongkie.visualmap.partition.PartitionController;
import org.mongkie.visualmap.partition.PartitionEvent;
import org.mongkie.visualmap.partition.PartitionModel;
import org.mongkie.visualmap.partition.PartitionModelListener;
import org.openide.util.Lookup;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class PartitionToolbar extends JToolBar implements PartitionModelListener {

    private transient PartitionController controller;
    private transient PartitionModel model;

    public PartitionToolbar() {
        this.controller = Lookup.getDefault().lookup(PartitionController.class);
        initComponents();
    }

    private void initComponents() {
        elementGroup = new ButtonGroup();
        for (final String elementType : controller.getElementTypes()) {
            JToggleButton elementButton = new JToggleButton();
            elementButton.setFocusPainted(false);
            elementButton.setText(elementType);
            elementButton.setEnabled(false);
            elementButton.addItemListener(new ItemListener() {

                @Override
                public void itemStateChanged(ItemEvent e) {
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        controller.setCurrentElementType(elementType);
                    }
                }
            });
//            elementButton.addActionListener(new ActionListener() {
//
//                @Override
//                public void actionPerformed(ActionEvent e) {
//                    controller.setCurrentElementType(elementType);
//                }
//            });
            elementGroup.add(elementButton);
            add(elementButton);
        }

        setFloatable(false);
        setRollover(true);
        Border b = (Border) UIManager.get("Nb.Editor.Toolbar.border"); //NOI18N
        setBorder(b);

        addSeparator();

        JLabel box = new JLabel();
        box.setMaximumSize(new java.awt.Dimension(32767, 32767));
        add(box);
    }
    private ButtonGroup elementGroup;

    @Override
    public void setEnabled(final boolean enabled) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                for (Component c : getComponents()) {
                    c.setEnabled(enabled);
                }
            }
        });
    }

    void refreshModel(PartitionModel model) {
        if (this.model != null) {
            this.model.removeModelListener(this);
        }
        this.model = model;
        if (model != null) {
            model.addModelListener(this);
        }

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                //init visual properties here?

                if (PartitionToolbar.this.model != null) {
                    //Select the right element group
                    refreshSelectedElement(PartitionToolbar.this.model.getCurrentElementType());
                } else {
                    elementGroup.clearSelection();
                }
            }
        });
    }

    /**
     * Refresh button selection for the selected element type
     *
     * @param elementType Selected element type, Nodes or Edges
     */
    private void refreshSelectedElement(String elementType) {
        ButtonModel buttonModel = null;
        Enumeration<AbstractButton> en = elementGroup.getElements();
        for (String elmtType : controller.getElementTypes()) {
            if (elmtType.equals(elementType)) {
                buttonModel = en.nextElement().getModel();
                break;
            }
            en.nextElement();
        }
        elementGroup.setSelected(buttonModel, true);
    }

    @Override
    public void processPartitionEvent(PartitionEvent e) {
        switch (e.getType()) {
            case CURRENT_ELEMENT_TYPE:
                refreshSelectedElement(e.getSource().getCurrentElementType());
                break;
            default:
                break;
        }

    }
}
