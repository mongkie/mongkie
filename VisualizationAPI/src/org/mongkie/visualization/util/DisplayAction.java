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
package org.mongkie.visualization.util;

import kobic.prefuse.display.DisplayListener;
import org.mongkie.util.SingleContextAction;
import org.mongkie.visualization.MongkieDisplay;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import prefuse.data.Graph;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public abstract class DisplayAction extends SingleContextAction<MongkieDisplay>
        implements DisplayListener<MongkieDisplay> {

    private MongkieDisplay current;

    protected DisplayAction() {
        this(Utilities.actionsGlobalContext());
    }

    protected DisplayAction(Lookup lookup) {
        super(MongkieDisplay.class, lookup);
    }

    @Override
    protected void contextChanged(MongkieDisplay d) {
        super.contextChanged(d);
        if (current != null) {
            current.removeDisplayListener(this);
        }
        if (d != null) {
            d.addDisplayListener(this);
        }
        current = d;
    }

    @Override
    public void graphDisposing(MongkieDisplay d, Graph g) {
        setEnabled(false);
    }

    @Override
    public void graphChanged(MongkieDisplay d, Graph g) {
        setEnabled(isEnabled(d));
    }
}
