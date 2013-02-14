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
package org.mongkie.ui.visualmap.ranking;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import org.mongkie.lib.widgets.DecoratedIcon;
import org.mongkie.visualmap.ranking.RankingController;
import org.mongkie.visualmap.ranking.RankingEvent;
import org.mongkie.visualmap.ranking.RankingModel;
import org.mongkie.visualmap.ranking.RankingModelListener;
import org.mongkie.visualmap.spi.ranking.Transformer;
import org.mongkie.visualmap.spi.ranking.TransformerUI;
import org.openide.util.ImageUtilities;

/**
 * 
 * @author Mathieu Bastian <mathieu.bastian@gephi.org>
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class RankingToolbar extends JToolBar implements RankingModelListener {

    private final RankingController controller;
    private RankingModel model;
    private final List<ButtonGroup> buttonGroups = new ArrayList<ButtonGroup>();

    public RankingToolbar(RankingController controller) {
        this.controller = controller;
        initComponents();
    }

    public void refreshModel(RankingModel model) {
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
                initTransformersUI();

                if (RankingToolbar.this.model != null) {
                    refreshSelectedTransformer();
                    //Select the right element group
                    refreshSelectedElement(RankingToolbar.this.model.getCurrentElementType());
                } else {
                    elementGroup.clearSelection();
                }
            }
        });
    }

    @Override
    public void processRankingEvent(RankingEvent e) {
        switch (e.getType()) {
            case CURRENT_ELEMENT_TYPE:
                refreshSelectedElement(e.getSource().getCurrentElementType());
                refreshSelectedTransformer();
                break;
            case CURRENT_TRANSFORMER:
                refreshSelectedTransformer();
                break;
            case START_AUTO_TRANSFORM:
            case STOP_AUTO_TRANSFORM:
                refreshDecoratedIcons();
                break;
            default:
                break;
        }
    }

    private void refreshSelectedTransformer() {
        //Select the right transformer
        int index = 0;
        for (String elmtType : controller.getElementTypes()) {
            ButtonGroup g = buttonGroups.get(index);
            boolean active = model == null ? false : model.getCurrentElementType().equals(elmtType);
            g.clearSelection();
            Transformer t = model.getCurrentTransformer(elmtType);
            String selected = model == null ? "" : controller.getUI(t).getDisplayName();
            for (Enumeration<AbstractButton> btns = g.getElements(); btns.hasMoreElements();) {
                AbstractButton btn = btns.nextElement();
                btn.setVisible(active);
                if (btn.getName().equals(selected)) {
                    g.setSelected(btn.getModel(), true);
                }
            }
            index++;
        }
    }

    private void refreshDecoratedIcons() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                int index = 0;
                for (String elementType : controller.getElementTypes()) {
                    ButtonGroup g = buttonGroups.get(index++);
                    boolean active = model == null ? false : model.getCurrentElementType().equals(elementType);
                    if (active) {
                        for (Enumeration<AbstractButton> btns = g.getElements(); btns.hasMoreElements();) {
                            btns.nextElement().repaint();
                        }
                    }
                }
            }
        });
    }

    private void refreshSelectedElement(String selected) {
        ButtonModel buttonModel = null;
        Enumeration<AbstractButton> en = elementGroup.getElements();
        for (String elmtType : controller.getElementTypes()) {
            if (elmtType.equals(selected)) {
                buttonModel = en.nextElement().getModel();
                break;
            }
            en.nextElement();
        }
        elementGroup.setSelected(buttonModel, true);
    }

    private void initTransformersUI() {
        //Clear present buttons
        for (ButtonGroup bg : buttonGroups) {
            for (Enumeration<AbstractButton> btns = bg.getElements(); btns.hasMoreElements();) {
                AbstractButton btn = btns.nextElement();
                remove(btn);
            }
        }
        buttonGroups.clear();
        if (model != null) {
            //Add transformers buttons, separate them by element group
            for (String elmtType : controller.getElementTypes()) {
                ButtonGroup buttonGroup = new ButtonGroup();
                for (final Transformer t : model.getTransformers(elmtType)) {
                    TransformerUI transformerUI = controller.getUI(t);
                    if (transformerUI != null) {
                        //Build button
                        Icon icon = transformerUI.getIcon();
                        DecoratedIcon decoratedIcon = getDecoratedIcon(icon, t);
                        JToggleButton btn = new JToggleButton(decoratedIcon);
                        btn.setToolTipText(transformerUI.getDisplayName());
                        btn.addActionListener(new ActionListener() {

                            @Override
                            public void actionPerformed(ActionEvent e) {
                                controller.setCurrentTransformer(t);
                            }
                        });
                        btn.setName(transformerUI.getDisplayName());
                        btn.setFocusPainted(false);
                        buttonGroup.add(btn);
                        add(btn);
                    }
                }
                buttonGroups.add(buttonGroup);
            }
        }
        revalidate();
        repaint();
    }

    private void initComponents() {
        elementGroup = new javax.swing.ButtonGroup();
        for (final String elementType : controller.getElementTypes()) {
            JToggleButton btn = new JToggleButton();
            btn.setFocusPainted(false);
            btn.setText(elementType);
            btn.setEnabled(false);
            btn.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    controller.setCurrentElementType(elementType);
                }
            });
            elementGroup.add(btn);
            add(btn);
        }

        setFloatable(false);
        setRollover(true);
        Border b = (Border) UIManager.get("Nb.Editor.Toolbar.border"); //NOI18N
        setBorder(b);

        addSeparator();
        add(new Box.Filler(new Dimension(0, 0), new Dimension(0, 0), new Dimension(32767, 0)));
//        javax.swing.JLabel box = new javax.swing.JLabel();
//        box.setMaximumSize(new java.awt.Dimension(32767, 32767));
//        add(box);
    }

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
    private javax.swing.ButtonGroup elementGroup;

    private DecoratedIcon getDecoratedIcon(Icon icon, final Transformer transformer) {
        Icon decoration = ImageUtilities.image2Icon(ImageUtilities.loadImage("org/mongkie/ui/visualmap/resources/chain.png", false));
        return new DecoratedIcon(icon, decoration, new DecoratedIcon.DecorationController() {

            @Override
            public boolean isDecorated() {
//                return model != null && model.isAutoTransformer(transformer);
                return false;
            }
        });
    }
}
