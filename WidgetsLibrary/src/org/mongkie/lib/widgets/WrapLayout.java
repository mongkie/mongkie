package org.mongkie.lib.widgets;

import java.awt.*;
import javax.swing.*;

/**
 * FlowLayout subclass that fully supports wrapping of components.
 */
public class WrapLayout extends FlowLayout {

    // The preferred size for this container.
    private Dimension preferredLayoutSize;

    /**
     * Constructs a new <code>WrapLayout</code> with a left
     * alignment and a default 5-unit horizontal and vertical gap.
     */
    public WrapLayout() {
        super(LEFT);
    }

    /**
     * Constructs a new <code>FlowLayout</code> with the specified
     * alignment and a default 5-unit horizontal and vertical gap.
     * The value of the alignment argument must be one of
     * <code>WrapLayout</code>, <code>WrapLayout</code>,
     * or <code>WrapLayout</code>.
     * @param align the alignment value
     */
    public WrapLayout(int align) {
        super(align);
    }

    /**
     * Creates a new flow layout manager with the indicated alignment
     * and the indicated horizontal and vertical gaps.
     * <p>
     * The value of the alignment argument must be one of
     * <code>WrapLayout</code>, <code>WrapLayout</code>,
     * or <code>WrapLayout</code>.
     * @param align the alignment value
     * @param hgap the horizontal gap between components
     * @param vgap the vertical gap between components
     */
    public WrapLayout(int align, int hgap, int vgap) {
        super(align, hgap, vgap);
    }

    /**
     * Returns the preferred dimensions for this layout given the
     * <i>visible</i> components in the specified target container.
     * @param target the component which needs to be laid out
     * @return the preferred dimensions to lay out the
     * subcomponents of the specified container
     */
    @Override
    public Dimension preferredLayoutSize(Container target) {
        return layoutSize(target, true);
    }

    /**
     * Returns the minimum dimensions needed to layout the <i>visible</i>
     * components contained in the specified target container.
     * @param target the component which needs to be laid out
     * @return the minimum dimensions to lay out the
     * subcomponents of the specified container
     */
    @Override
    public Dimension minimumLayoutSize(Container target) {
        return layoutSize(target, false);
    }

    /**
     * Returns the minimum or preferred dimension needed to layout the target
     * container.
     *
     * @param target target to get layout size for
     * @param preferred should preferred size be calculated
     * @return the dimension to layout the target container
     */
    private Dimension layoutSize(Container target, boolean preferred) {
        synchronized (target.getTreeLock()) {
            //  Each row must fit with the width allocated to the containter.
            //  When the container width = 0, the preferred width of the container
            //  has not yet been calculated so lets ask for the maximum.

            int targetWidth = target.getSize().width;

            if (targetWidth == 0) {
                targetWidth = Integer.MAX_VALUE;
            }

            int hgap = getHgap();
            int vgap = getVgap();
            Insets insets = target.getInsets();
            int maxWidth = targetWidth - (insets.left + insets.right + hgap * 2);

            //  Fit components into the allowed width

            Dimension dim = new Dimension(0, 0);
            int rowWidth = 0;
            int rowHeight = 0;

            int nmembers = target.getComponentCount();

            for (int i = 0; i < nmembers; i++) {
                Component m = target.getComponent(i);

                if (m.isVisible()) {
                    Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();

                    if (rowWidth + d.width > maxWidth) {
                        addRow(dim, rowWidth, rowHeight);
                        rowWidth = 0;
                        rowHeight = 0;
                    }

                    if (rowWidth > 0) {
                        rowWidth += hgap;
                    }

                    rowWidth += d.width;
                    rowHeight = Math.max(rowHeight, d.height);
                }
            }

            addRow(dim, rowWidth, rowHeight);

            dim.width += insets.left + insets.right + hgap * 2;
            dim.height += insets.top + insets.bottom + vgap * 2;

            return dim;
        }
    }

    /*
     * Add a new row to the given dimension.
     *
     * @param dim dimension to add row to
     * @param rowWidth the width of the row to add
     * @param rowHeight the height of the row to add
     */
    private void addRow(Dimension dim, int rowWidth, int rowHeight) {
        dim.width = Math.max(dim.width, rowWidth);

        if (dim.height > 0) {
            dim.height += getVgap();
        }

        dim.height += rowHeight;
    }

    /**
     * Lays out the container. This method lets each component take
     * its preferred size by reshaping the components in the
     * target container in order to satisfy the alignment of
     * this <code>WrapLayout</code> object.
     * @param target the specified component being laid out
     */
    @Override
    public void layoutContainer(final Container target) {
        Dimension size = preferredLayoutSize(target);

        if (size.equals(preferredLayoutSize)) {
            super.layoutContainer(target);
        } else {
            preferredLayoutSize = size;
            target.invalidate();
            Container top = target;

//			while (top.getParent() != null)
            if (top.getParent() != null) {
                top = top.getParent();
            }

            top.validate();
        }
    }

    public static void main(String[] args) {
        final JPanel buttons = new JPanel();
        buttons.setLayout(new WrapLayout());
        buttons.add(new JButton("long long button1"));
        buttons.add(new JButton("button2"));
        buttons.add(new JButton("button3"));
        buttons.add(new JButton("button4"));
        buttons.add(new JButton("button5"));
        buttons.add(new JButton("button6"));
        buttons.add(new JButton("button7"));
        buttons.add(new JButton("button8"));
        buttons.add(new JButton("button9"));

        // Uncomment to limit initial width of the panel
//		buttons.setSize(new Dimension(300, 1) );

        JPanel red = new JPanel();
        red.setBackground(Color.red);
        red.setPreferredSize(new Dimension(300, 200));
//
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(buttons, BorderLayout.NORTH);
        frame.getContentPane().add(red);

        //  Uncomment the following block to see how the button panel
        //  operates in a scroll pane.
/*
        final JScrollPane scrollPane = new JScrollPane(buttons);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setPreferredSize( new Dimension(200, 150) );
        buttons.setSize(scrollPane.getPreferredSize());
        scrollPane.addComponentListener( new java.awt.event.ComponentAdapter()
        {
        public void componentResized(java.awt.event.ComponentEvent e)
        {
        JScrollPane scrollPane = (JScrollPane)e.getComponent();
        JViewport viewport = scrollPane.getViewport();
        Dimension d = viewport.getSize();
        viewport.getView().setSize(d);
        }
        });
        frame.getContentPane().add(scrollPane);
         */
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
