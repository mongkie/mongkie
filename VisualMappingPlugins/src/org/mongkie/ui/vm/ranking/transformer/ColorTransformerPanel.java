/*
 Copyright 2008-2010 Gephi
 Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
 Website : http://www.gephi.org

 This file is part of Gephi.

 Gephi is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as
 published by the Free Software Foundation, either version 3 of the
 License, or (at your option) any later version.

 Gephi is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.mongkie.ui.vm.ranking.transformer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DecimalFormat;
import java.util.Arrays;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.mongkie.lib.widgets.Palette;
import org.mongkie.lib.widgets.PaletteIcon;
import org.mongkie.lib.widgets.slider.GradientSlider;
import org.mongkie.lib.widgets.slider.JRangeSlider;
import org.mongkie.visualmap.spi.ranking.AbstractColorTransformer;
import org.mongkie.visualmap.spi.ranking.Ranking;
import org.mongkie.visualmap.spi.ranking.Transformer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 * @author Mathieu Bastian
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class ColorTransformerPanel extends javax.swing.JPanel {

    private static final int SLIDER_MAXIMUM = 100;
    private final AbstractColorTransformer colorTransformer;
    private Ranking ranking;
    private final GradientSlider gradientSlider;
    private final RecentPalettes recentPalettes;

    public ColorTransformerPanel(Transformer transformer, Ranking ranking) {
        initComponents();

        final String POSITIONS = "ColorTransformerPanel_" + transformer.getClass().getSimpleName() + "@" + ranking.getElementType() + "_positions";
        final String COLORS = "ColorTransformerPanel_" + transformer.getClass().getSimpleName() + "@" + ranking.getElementType() + "_colors";

        colorTransformer = (AbstractColorTransformer) transformer;
        this.ranking = ranking;
        this.recentPalettes = new RecentPalettes();

        float[] positionsStart = colorTransformer.getColorPositions();
        Color[] colorsStart = colorTransformer.getColors();

        try {
            positionsStart = deserializePositions(NbPreferences.forModule(ColorTransformerPanel.class).getByteArray(POSITIONS, serializePositions(positionsStart)));
            colorsStart = deserializeColors(NbPreferences.forModule(ColorTransformerPanel.class).getByteArray(COLORS, serializeColors(colorsStart)));
            colorTransformer.setColorPositions(positionsStart);
            colorTransformer.setColors(colorsStart);
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
        //Gradient
        gradientSlider = new GradientSlider(GradientSlider.HORIZONTAL, positionsStart, colorsStart);
        gradientSlider.putClientProperty("GradientSlider.includeOpacity", "false");
        gradientSlider.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                Color[] colors = gradientSlider.getColors();
                float[] positions = gradientSlider.getThumbPositions();
                colorTransformer.setColors(Arrays.copyOf(colors, colors.length));
                colorTransformer.setColorPositions(Arrays.copyOf(positions, positions.length));
                try {
                    NbPreferences.forModule(ColorTransformerPanel.class).putByteArray(POSITIONS, serializePositions(positions));
                    NbPreferences.forModule(ColorTransformerPanel.class).putByteArray(COLORS, serializeColors(colors));
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
                prepareGradientTooltip();
            }
        });
        gradientPanel.add(gradientSlider, BorderLayout.CENTER);

        //Range
        JRangeSlider slider = (JRangeSlider) rangeSlider;
        slider.setMinimum(0);
        slider.setMaximum(SLIDER_MAXIMUM);
        slider.setValue(0);
        slider.setUpperValue(SLIDER_MAXIMUM);
        slider.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                JRangeSlider source = (JRangeSlider) e.getSource();
                if (!source.getValueIsAdjusting()) {
                    setRangeValues();
                }
                prepareGradientTooltip();
            }
        });

        refreshRangeValues();
        prepareGradientTooltip();

        //Context
//        setComponentPopupMenu(getPalettePopupMenu());
        addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent evt) {
                if (evt.isPopupTrigger()) {
                    JPopupMenu popupMenu = getPalettePopupMenu();
                    popupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
                }
            }

            @Override
            public void mouseReleased(MouseEvent evt) {
                if (evt.isPopupTrigger()) {
                    JPopupMenu popupMenu = getPalettePopupMenu();
                    popupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
                }
            }
        });

        //Color Swatch
        showPaletteButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                JPopupMenu popupMenu = getPalettePopupMenu();
                popupMenu.show(paletteToolbar, 0, paletteToolbar.getHeight());
            }
        });
        invertButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                gradientSlider.setValues(invert(gradientSlider.getThumbPositions()), invert(gradientSlider.getColors()));
            }
        });
    }

    void refresh(Ranking ranking) {
        this.ranking = ranking;
        if (ranking != null) {
            refreshRangeValues();
            prepareGradientTooltip();
        }
    }

    private void prepareGradientTooltip() {
        StringBuilder sb = new StringBuilder();
        final double min = ((Number) ranking.unnormalize(colorTransformer.getLowerBound())).doubleValue();
        final double max = ((Number) ranking.unnormalize(colorTransformer.getUpperBound())).doubleValue();
        final double range = max - min;
        float[] positions = gradientSlider.getThumbPositions();
        for (int i = 0; i < positions.length - 1; i++) {
            sb.append(min + range * positions[i]);
            sb.append(", ");
        }
        sb.append(min + range * positions[positions.length - 1]);
        gradientSlider.setToolTipText(sb.toString());
    }

    private void setRangeValues() {
        JRangeSlider slider = (JRangeSlider) rangeSlider;
        float low = slider.getValue() / 100f;
        float high = slider.getUpperValue() / 100f;
        colorTransformer.setLowerBound(low);
        colorTransformer.setUpperBound(high);

        lowerBoundLabel.setText(new DecimalFormat("###.##").format(ranking.unnormalize(colorTransformer.getLowerBound())));
        upperBoundLabel.setText(new DecimalFormat("###.##").format(ranking.unnormalize(colorTransformer.getUpperBound())));
    }

    private void refreshRangeValues() {
        JRangeSlider slider = (JRangeSlider) rangeSlider;
        slider.setValue((int) (colorTransformer.getLowerBound() * 100f));
        slider.setUpperValue((int) (colorTransformer.getUpperBound() * 100f));

        lowerBoundLabel.setText(new DecimalFormat("###.##").format(ranking.unnormalize(colorTransformer.getLowerBound())));
        upperBoundLabel.setText(new DecimalFormat("###.##").format(ranking.unnormalize(colorTransformer.getUpperBound())));
    }

    private JPopupMenu getPalettePopupMenu() {
        JPopupMenu popupMenu = new JPopupMenu();
        JMenu palettes = new JMenu(NbBundle.getMessage(ColorTransformerPanel.class, "ColorTransformerPanel.palettePopup.default"));
        for (Palette p : Palette.getSequencialPalettes()) {
            final Palette p3 = Palette.get3ClassPalette(p);
            JMenuItem item = new JMenuItem(new PaletteIcon(p3.getColors()));
            item.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    gradientSlider.setValues(p3.getPositions(), p3.getColors());
                }
            });
            palettes.add(item);
        }
        for (Palette p : Palette.getDivergingPalettes()) {
            final Palette p3 = Palette.get3ClassPalette(p);
            JMenuItem item = new JMenuItem(new PaletteIcon(p3.getColors()));
            item.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    gradientSlider.setValues(p3.getPositions(), p3.getColors());
                }
            });
            palettes.add(item);
        }
        popupMenu.add(palettes);

        //Recently Used
        JMenu recentMenu = new JMenu(NbBundle.getMessage(ColorTransformerPanel.class, "ColorTransformerPanel.palettePopup.recent"));
        for (final AbstractColorTransformer.LinearGradient gradient : recentPalettes.getPalettes()) {
            JMenuItem item = new JMenuItem(new PaletteIcon(gradient.getColors()));
            item.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    gradientSlider.setValues(gradient.getPositions(), gradient.getColors());
                }
            });
            recentMenu.add(item);
        }
        popupMenu.add(recentMenu);

        //Invert
        JMenuItem invertItem = new JMenuItem(NbBundle.getMessage(ColorTransformerPanel.class, "ColorTransformerPanel.palettePopup.invert"));
        invertItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                gradientSlider.setValues(invert(gradientSlider.getThumbPositions()), invert(gradientSlider.getColors()));
            }
        });
        popupMenu.add(invertItem);

        return popupMenu;
    }

    void addRecentPalette() {
        AbstractColorTransformer.LinearGradient gradient = colorTransformer.getLinearGradient();
        recentPalettes.add(gradient);
    }

    private Color[] invert(Color[] source) {
        int len = source.length;
        Color[] res = new Color[len];
        for (int i = 0; i < len; i++) {
            res[i] = source[len - 1 - i];
        }
        return res;
    }

    private float[] invert(float[] source) {
        int len = source.length;
        float[] res = new float[len];
        for (int i = 0; i < len; i++) {
            res[i] = 1 - source[len - 1 - i];
        }

        return res;
    }

    private byte[] serializePositions(float[] positions) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(positions);
        out.close();
        return bos.toByteArray();
    }

    private float[] deserializePositions(byte[] positions) throws Exception {
        ByteArrayInputStream bis = new ByteArrayInputStream(positions);
        ObjectInputStream in = new ObjectInputStream(bis);
        float[] array = (float[]) in.readObject();
        in.close();
        return array;
    }

    private byte[] serializeColors(Color[] colors) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(colors);
        out.close();
        return bos.toByteArray();
    }

    private Color[] deserializeColors(byte[] colors) throws Exception {
        ByteArrayInputStream bis = new ByteArrayInputStream(colors);
        ObjectInputStream in = new ObjectInputStream(bis);
        Color[] array = (Color[]) in.readObject();
        in.close();
        return array;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        labelColor = new javax.swing.JLabel();
        gradientPanel = new javax.swing.JPanel();
        rangeSlider = new JRangeSlider();
        labelRange = new javax.swing.JLabel();
        upperBoundLabel = new javax.swing.JLabel();
        lowerBoundLabel = new javax.swing.JLabel();
        paletteToolbar = new javax.swing.JToolBar();
        showPaletteButton = new javax.swing.JButton();
        invertButton = new javax.swing.JButton();

        labelColor.setText(org.openide.util.NbBundle.getMessage(ColorTransformerPanel.class, "ColorTransformerPanel.labelColor.text")); // NOI18N

        gradientPanel.setOpaque(false);
        gradientPanel.setLayout(new java.awt.BorderLayout());

        rangeSlider.setFocusable(false);
        rangeSlider.setOpaque(false);

        labelRange.setText(org.openide.util.NbBundle.getMessage(ColorTransformerPanel.class, "ColorTransformerPanel.labelRange.text")); // NOI18N

        upperBoundLabel.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        upperBoundLabel.setForeground(new java.awt.Color(102, 102, 102));
        upperBoundLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        upperBoundLabel.setText(org.openide.util.NbBundle.getMessage(ColorTransformerPanel.class, "ColorTransformerPanel.upperBoundLabel.text")); // NOI18N

        lowerBoundLabel.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        lowerBoundLabel.setForeground(new java.awt.Color(102, 102, 102));
        lowerBoundLabel.setText(org.openide.util.NbBundle.getMessage(ColorTransformerPanel.class, "ColorTransformerPanel.lowerBoundLabel.text")); // NOI18N

        paletteToolbar.setFloatable(false);
        paletteToolbar.setRollover(true);
        paletteToolbar.setOpaque(false);

        showPaletteButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/mongkie/ui/vm/resources/color-swatch-small.png"))); // NOI18N
        showPaletteButton.setToolTipText(org.openide.util.NbBundle.getMessage(ColorTransformerPanel.class, "ColorTransformerPanel.showPaletteButton.toolTipText")); // NOI18N
        showPaletteButton.setBorderPainted(false);
        showPaletteButton.setFocusPainted(false);
        showPaletteButton.setFocusable(false);
        showPaletteButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        showPaletteButton.setIconTextGap(0);
        showPaletteButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        paletteToolbar.add(showPaletteButton);

        invertButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/mongkie/ui/vm/resources/contrast-small.png"))); // NOI18N
        invertButton.setToolTipText(org.openide.util.NbBundle.getMessage(ColorTransformerPanel.class, "ColorTransformerPanel.invertButton.toolTipText")); // NOI18N
        invertButton.setBorderPainted(false);
        invertButton.setFocusPainted(false);
        invertButton.setFocusable(false);
        invertButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        invertButton.setIconTextGap(0);
        invertButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        paletteToolbar.add(invertButton);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labelColor)
                    .addComponent(labelRange))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lowerBoundLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(upperBoundLabel))
                    .addComponent(gradientPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 172, Short.MAX_VALUE)
                    .addComponent(rangeSlider, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(paletteToolbar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(paletteToolbar, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(25, 25, 25)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(gradientPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(labelColor, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(rangeSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelRange, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lowerBoundLabel)
                    .addComponent(upperBoundLabel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel gradientPanel;
    private javax.swing.JButton invertButton;
    private javax.swing.JLabel labelColor;
    private javax.swing.JLabel labelRange;
    private javax.swing.JLabel lowerBoundLabel;
    private javax.swing.JToolBar paletteToolbar;
    private javax.swing.JSlider rangeSlider;
    private javax.swing.JButton showPaletteButton;
    private javax.swing.JLabel upperBoundLabel;
    // End of variables declaration//GEN-END:variables
}
