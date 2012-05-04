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

/*
 * RankingChooser.java
 *
 * Created on Sep 8, 2011, 5:01:14 PM
 */
package org.mongkie.ui.visualmap.ranking;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.Point2D;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import org.mongkie.lib.widgets.spline.SplineEditor;
import org.mongkie.visualmap.ranking.Interpolator;
import org.mongkie.visualmap.spi.ranking.Ranking;
import org.mongkie.visualmap.ranking.RankingController;
import org.mongkie.visualmap.ranking.RankingEvent;
import org.mongkie.visualmap.ranking.RankingModel;
import org.mongkie.visualmap.ranking.RankingModelListener;
import org.mongkie.visualmap.spi.ranking.Transformer;
import org.mongkie.visualmap.spi.ranking.TransformerUI;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 *
 * @author Mathieu Bastian <mathieu.bastian@gephi.org>
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class RankingChooser extends javax.swing.JPanel implements ItemListener, RankingModelListener {

    private final String NO_SELECTION;
    private final RankingController controller;
    private RankingModel model;
    private javax.swing.JPanel transformerPanel;
    private SplineEditor splineEditor;
    private Interpolator interpolator;

    /** Creates new form RankingChooser */
    public RankingChooser(RankingController controller) {
        this.controller = controller;
        NO_SELECTION = NbBundle.getMessage(RankingChooser.class, "RankingChooser.rankingComboBox.chooseParameter.text");

        initComponents();
        initControlEvents();

        final ListCellRenderer defaultRenderer = rankingComboBox.getRenderer();
        rankingComboBox.setRenderer(new DefaultListCellRenderer() {

            @Override
            public java.awt.Component getListCellRendererComponent(javax.swing.JList jlist, Object o, int i, boolean bln, boolean bln1) {
                if (o instanceof Ranking) {
                    return defaultRenderer.getListCellRendererComponent(jlist, ((Ranking) o).getDisplayName(), i, bln, bln1);
                } else {
                    return defaultRenderer.getListCellRendererComponent(jlist, o, i, bln, bln1);
                }
            }
        });
    }

    private void initControlEvents() {
        applyButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Transformer transformer = model.getCurrentTransformer();
                if (transformer != null) {
                    if (interpolator != null) {
                        controller.setInterpolator(new Interpolator() {

                            @Override
                            public float interpolate(float x) {
                                return interpolator.interpolate(x);
                            }
                        });
                    }
                    controller.transform(model.getCurrentRanking(), transformer);
                }
            }
        });
        applyButton.setVisible(false);

        splineButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (splineEditor == null) {
                    splineEditor = new SplineEditor(WindowManager.getDefault().getMainWindow(),
                            NbBundle.getMessage(RankingChooser.class, "RankingChooser.splineEditor.title"));
                }
                splineEditor.setVisible(true);
                Point2D control1 = splineEditor.getCurrentControl1();
                Point2D control2 = splineEditor.getCurrentControl2();
                interpolator = new Interpolator.BezierInterpolator(
                        (float) control1.getX(), (float) control1.getY(),
                        (float) control2.getX(), (float) control2.getY());
            }
        });
        splineButton.setVisible(false);
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED && model != null) {
            if (!rankingComboBox.getSelectedItem().equals(NO_SELECTION)) {
                controller.setCurrentRanking((Ranking) rankingComboBox.getSelectedItem());
            } else {
                controller.setCurrentRanking(null);
            }
        }
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
                refreshModel();
            }
        });
    }

    private void refreshModel() {
        if (transformerPanel != null) {
            remove(transformerPanel);
        }
        applyButton.setVisible(false);
        splineButton.setVisible(false);

        Ranking selectedRanking = refreshComboBox();
        if (selectedRanking != null) {
            refreshTransformerPanel(selectedRanking);
        }

        revalidate();
        repaint();
    }

    private Ranking refreshComboBox() {
        rankingComboBox.removeItemListener(this);
        final DefaultComboBoxModel comboBoxModel = new DefaultComboBoxModel();
        comboBoxModel.addElement(NO_SELECTION);
        comboBoxModel.setSelectedItem(NO_SELECTION);
        Ranking selectedRanking = null;
        if (model != null) {
            selectedRanking = model.getCurrentRanking();
            for (Ranking r : model.getRankings()) {
                comboBoxModel.addElement(r);
                if (selectedRanking != null
                        && selectedRanking.getElementType().equals(r.getElementType()) && selectedRanking.getName().equals(r.getName())) {
                    comboBoxModel.setSelectedItem(r);
                }
            }
            selectedRanking = model.getCurrentRanking(); // May have been refresh by the model
            rankingComboBox.addItemListener(this);
        }
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                rankingComboBox.setModel(comboBoxModel);
            }
        });
        return selectedRanking;
    }

    private void refreshTransformerPanel(Ranking selectedRanking) {
        Transformer transformer = model.getCurrentTransformer();
        TransformerUI transformerUI = controller.getUI(transformer);
        if (!Double.isNaN(selectedRanking.getMinimumValue().doubleValue())
                && !Double.isNaN(selectedRanking.getMaximumValue().doubleValue())
                && selectedRanking.getMinimumValue() != selectedRanking.getMaximumValue()) {
            applyButton.setEnabled(true);
        } else {
            applyButton.setEnabled(false);
        }
        transformerPanel = transformerUI.getPanel(transformer, selectedRanking);
        transformerPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5), BorderFactory.createEtchedBorder()));
        transformerPanel.setOpaque(false);
        add(transformerPanel, BorderLayout.CENTER);
        splineButton.setVisible(true);
        applyButton.setVisible(true);
    }

    @Override
    public void processRankingEvent(RankingEvent e) {
        switch (e.getType()) {
            case CURRENT_ELEMENT_TYPE:
                refreshModel();
                break;
            case CURRENT_RANKING:
            case CURRENT_TRANSFORMER:
                final Ranking selectedRanking = model.getCurrentRanking();
                if (transformerPanel != null) {
                    remove(transformerPanel);
                }
                applyButton.setVisible(false);
                splineButton.setVisible(false);
                if (selectedRanking != null) {
                    refreshTransformerPanel(selectedRanking);
                    if (rankingComboBox.getSelectedItem() != selectedRanking) {
                        refreshComboBox();
                    }
                }
                revalidate();
                repaint();
                break;
            case REFRESH_RANKING:
                refreshComboBox();
                break;
            default:
                break;
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        rankingComboBox.setEnabled(enabled);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        chooserPanel = new javax.swing.JPanel();
        rankingComboBox = new javax.swing.JComboBox();
        controlPanel = new javax.swing.JPanel();
        applyButton = new javax.swing.JButton();
        splineButton = new org.jdesktop.swingx.JXHyperlink();

        setOpaque(false);
        setLayout(new java.awt.BorderLayout());

        chooserPanel.setOpaque(false);
        chooserPanel.setLayout(new java.awt.GridBagLayout());

        rankingComboBox.setToolTipText(org.openide.util.NbBundle.getMessage(RankingChooser.class, "RankingChooser.rankingComboBox.toolTipText")); // NOI18N
        rankingComboBox.setOpaque(false);
        rankingComboBox.setPreferredSize(new java.awt.Dimension(56, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 1, 5, 1);
        chooserPanel.add(rankingComboBox, gridBagConstraints);

        add(chooserPanel, java.awt.BorderLayout.PAGE_START);

        controlPanel.setOpaque(false);
        controlPanel.setLayout(new java.awt.GridBagLayout());

        applyButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/mongkie/ui/visualmap/resources/apply.gif"))); // NOI18N
        applyButton.setText(org.openide.util.NbBundle.getMessage(RankingChooser.class, "RankingChooser.applyButton.text")); // NOI18N
        applyButton.setToolTipText(org.openide.util.NbBundle.getMessage(RankingChooser.class, "RankingChooser.applyButton.toolTipText")); // NOI18N
        applyButton.setFocusPainted(false);
        applyButton.setMargin(new java.awt.Insets(0, 8, 0, 8));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(2, 18, 2, 5);
        controlPanel.add(applyButton, gridBagConstraints);

        splineButton.setClickedColor(new java.awt.Color(0, 51, 255));
        splineButton.setText(org.openide.util.NbBundle.getMessage(RankingChooser.class, "RankingChooser.splineButton.text")); // NOI18N
        splineButton.setToolTipText(org.openide.util.NbBundle.getMessage(RankingChooser.class, "RankingChooser.splineButton.toolTipText")); // NOI18N
        splineButton.setFocusPainted(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        controlPanel.add(splineButton, gridBagConstraints);

        add(controlPanel, java.awt.BorderLayout.PAGE_END);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton applyButton;
    private javax.swing.JPanel chooserPanel;
    private javax.swing.JPanel controlPanel;
    private javax.swing.JComboBox rankingComboBox;
    private org.jdesktop.swingx.JXHyperlink splineButton;
    // End of variables declaration//GEN-END:variables
}
