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

import org.openide.WizardDescriptor;

/**
 * 
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class ImportCSVNodeTableWizardPanel extends ImportCSVWizardPanel {

    @Override
    protected ImportCSVInnerPanel createInnerPanel() {
        return new ImportCSVNodeTableInnerPanel(this);
    }

    @Override
    protected void setup(WizardDescriptor wizardDescriptor) {
    }

    @Override
    protected void apply(WizardDescriptor wizardDescriptor) {
        ImportCSVNodeTableInnerPanel panel = (ImportCSVNodeTableInnerPanel) getInnerPanel();
        wizardDescriptor.putProperty(PROP_NODETABLE_CSV_FILE, panel.getSelectedFile());
        wizardDescriptor.putProperty(PROP_NODETABLE_ID_COLUMN, panel.getSelectedIdColumn());
        wizardDescriptor.putProperty(PROP_NODETABLE_LABEL_COLUMN, panel.getSelectedLabelColumn());
        wizardDescriptor.putProperty(PROP_NODETABLE_COLUMNS, panel.getHeaderNames());
        wizardDescriptor.putProperty(PROP_NODETABLE_HAS_HEADER, panel.hasHeader());
    }
}
