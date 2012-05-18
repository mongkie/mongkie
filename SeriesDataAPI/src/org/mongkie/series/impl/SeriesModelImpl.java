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
package org.mongkie.series.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mongkie.longtask.LongTask;
import org.mongkie.longtask.LongTaskErrorHandler;
import org.mongkie.longtask.LongTaskExecutor;
import org.mongkie.longtask.LongTaskListener;
import org.mongkie.series.SeriesData;
import org.mongkie.series.SeriesModel;
import org.mongkie.series.SeriesModelListener;
import org.mongkie.visualization.MongkieDisplay;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class SeriesModelImpl implements SeriesModel {

    private SeriesData series;
    private final List<SeriesModelListener> listeners;
    private LongTaskExecutor executor;
    private final MongkieDisplay display;

    SeriesModelImpl(MongkieDisplay display) {
        this.display = display;
        series = new SeriesData();
        listeners = new ArrayList<SeriesModelListener>();
        executor = new LongTaskExecutor(true, "SeriesLoader");
        executor.setLongTaskListener(new LongTaskListener() {

            @Override
            public void taskFinished(LongTask task) {
                setLoading(false);
            }

            @Override
            public void taskStarted(LongTask task) {
                setLoading(true);
            }
        });
        executor.setDefaultErrorHandler(new LongTaskErrorHandler() {

            @Override
            public void fatalError(Throwable t) {
                Logger.getLogger("").log(Level.SEVERE, "", t.getCause() != null ? t.getCause() : t);
            }
        });
    }

    @Override
    public MongkieDisplay getDisplay() {
        return display;
    }

    @Override
    public SeriesData getData() {
        return series;
    }

    void setData(SeriesData series) {
        SeriesData old = this.series;
        this.series = series;
        for (SeriesModelListener l : listeners) {
            l.seriesChanged(display.getGraph(), old, series);
        }
        display.fireGraphChangedEvent();
    }

    void clearData() {
        if (isEmpty()) {
            return;
        }
        series.clear();
        for (SeriesModelListener l : listeners) {
            l.seriesCleared();
        }
    }

    @Override
    public boolean isLoading() {
        return executor.isRunning();
    }

    void setLoading(boolean loading) {
        for (SeriesModelListener l : listeners) {
            if (loading) {
                l.loadingStarted();
            } else {
                l.loadingFinished();
            }
        }
    }

    @Override
    public boolean isEmpty() {
        return series.isEmpty();
    }

    LongTaskExecutor getExecutor() {
        return executor;
    }

    @Override
    public void addModelListener(SeriesModelListener l) {
        if (!listeners.contains(l)) {
            listeners.add(l);
        }
    }

    @Override
    public void removeModelListener(SeriesModelListener l) {
        listeners.remove(l);
    }
}
