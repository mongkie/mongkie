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
package org.mongkie.ui.importer.csv;

import java.awt.Component;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public abstract class ImportCSVWizardPanel implements WizardDescriptor.Panel {

    public static final String PROP_NODETABLE_CSV_FILE = "ImportCSV.nodeTable.csvFile";
    public static final String PROP_NODETABLE_ID_COLUMN = "ImportCSV.nodeTable.idColumn";
    public static final String PROP_NODETABLE_LABEL_COLUMN = "ImportCSV.nodeTable.labelColumn";
    public static final String PROP_NODETABLE_COLUMNS = "ImportCSV.nodeTable.columnNames";
    public static final String PROP_NODETABLE_HAS_HEADER = "ImportCSV.nodeTable.hasHeader";
    public static final String PROP_EDGETABLE_CSV_FILE = "ImportCSV.edgeTable.csvFile";
    public static final String PROP_EDGETABLE_SOURCE_COLUMN = "ImportCSV.edgeTable.sourceColumn";
    public static final String PROP_EDGETABLE_TARGET_COLUMN = "ImportCSV.edgeTable.targetColumn";
    public static final String PROP_EDGETABLE_LABEL_COLUMN = "ImportCSV.edgeTable.labelColumn";
    public static final String PROP_EDGETABLE_IS_DIRECTED = "ImportCSV.edgeTable.isDirected";
    public static final String PROP_EDGETABLE_COLUMNS = "ImportCSV.edgeTable.columnNames";
    public static final String PROP_EDGETABLE_HAS_HEADER = "ImportCSV.edgeTable.hasHeader";
    private WizardDescriptor wizardDescriptor;
    /**
     * The visual panel that displays this panel. If you need to access the
     * panel from this class, just use getComponent().
     */
    private ImportCSVInnerPanel panel;

    /**
     * Get the visual panel for the panel. In this template, the panel
     * is kept separate. This can be more efficient: if the wizard is created
     * but never displayed, or not all panels are displayed, it is better to
     * create only those which really need to be visible.
     * 
     * @return The visual(validation) panel for the wizard panel
     */
    @Override
    public Component getComponent() {
        return getInnerPanel().getValidationPanel();
    }

    protected abstract ImportCSVInnerPanel createInnerPanel();

    protected ImportCSVInnerPanel getInnerPanel() {
        if (panel == null) {
            panel = createInnerPanel();
        }
        return panel;
    }

    @Override
    public HelpCtx getHelp() {
        // Show no Help button for this panel:
        return HelpCtx.DEFAULT_HELP;
        // If you have context help:
        // return new HelpCtx(ImportCSVNodeTableWizardPanel.class);
    }

    // You can use a settings object to keep track of state. Normally the
    // settings object will be the WizardDescriptor, so you can use
    // WizardDescriptor.getProperty & putProperty to store information entered
    // by the user.
    @Override
    public final void readSettings(Object settings) {
        wizardDescriptor = ((WizardDescriptor) settings);
        setup(wizardDescriptor);
    }

    protected abstract void setup(WizardDescriptor wizardDescriptor);

    @Override
    public final void storeSettings(Object settings) {
        apply((WizardDescriptor) settings);
    }

    protected abstract void apply(WizardDescriptor wizardDescriptor);

    public WizardDescriptor getWizardDescriptor() {
        return wizardDescriptor;
    }

    @Override
    public boolean isValid() {
        return !panel.isProblem();
    }

    @Override
    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    @Override
    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    protected final void fireChangeEvent() {
        Iterator<ChangeListener> it;
        synchronized (listeners) {
            it = new HashSet<ChangeListener>(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            it.next().stateChanged(ev);
        }
    }
    private final Set<ChangeListener> listeners = new HashSet<ChangeListener>(1); // or can use ChangeSupport in NB 6.0
}
