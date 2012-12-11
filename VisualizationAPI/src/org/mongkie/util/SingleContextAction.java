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
package org.mongkie.util;

import java.awt.event.ActionEvent;
import java.util.Collection;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 * @param <T>
 */
public abstract class SingleContextAction<T> extends AbstractAction
        implements ContextAwareAction, LookupListener {

    private final Lookup.Result<T> result;

    protected SingleContextAction(Class<T> type, Lookup lookup) {
        result = lookup.lookupResult(type);
        result.addLookupListener(SingleContextAction.this);
        resultChanged(null);
    }

    @Override
    public final void resultChanged(LookupEvent ev) {
        Collection<? extends T> contexts = result.allInstances();
        contextChanged(contexts.isEmpty() ? null : contexts.iterator().next());
    }

    protected void contextChanged(T context) {
        setEnabled(context != null && isEnabled(context));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        performAction(result.allInstances().iterator().next());
    }

    protected abstract boolean isEnabled(T context);

    protected abstract void performAction(T context);

    @Override
    public abstract Action createContextAwareInstance(Lookup lookup);
}
