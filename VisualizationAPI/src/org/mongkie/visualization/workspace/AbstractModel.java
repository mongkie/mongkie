/*
 * This file is part of MONGKIE. Visit <http://www.mongkie.org/> for details.
 * Copyright (C) 2011 Korean Bioinformation Center(KOBIC)
 *
 * MONGKIE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MONGKE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.mongkie.visualization.workspace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.mongkie.visualization.MongkieDisplay;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 * @param <V>
 * @param <L>
 */
public abstract class AbstractModel<V, L extends ModelListener> implements Model<V, L> {

    private final MongkieDisplay display;
    protected final List<L> listeners;
    private V data;

    public AbstractModel(MongkieDisplay display) {
        this.display = display;
        listeners = Collections.synchronizedList(new ArrayList<L>());
    }

    @Override
    public MongkieDisplay getDisplay() {
        return display;
    }

    @Override
    public void addModelListener(L l) {
        if (!listeners.contains(l)) {
            listeners.add(l);
        }
    }

    @Override
    public void removeModelListener(L l) {
        listeners.remove(l);
    }

    @Override
    public V get() {
        return data;
    }

    protected void set(V data) {
        if (this.data != data) {
            V old = this.data;
            this.data = data;
            if (old != null) {
                unload(old);
            }
            if (data != null) {
                load(data);
            }
            changed(old, data);
        }
    }

    protected abstract void changed(V o, V n);

    protected abstract void load(V data);

    protected abstract void unload(V data);
}
