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
package org.mongkie.enrichment.impl;

import java.util.HashMap;
import java.util.Map;
import org.mongkie.enrichment.EnrichmentModel;
import org.mongkie.enrichment.EnrichmentModelListener;
import org.mongkie.enrichment.spi.EnrichedResultUI;
import org.mongkie.enrichment.spi.Enrichment;
import org.mongkie.longtask.LongTask;
import org.mongkie.longtask.LongTaskErrorHandler;
import org.mongkie.longtask.LongTaskExecutor;
import org.mongkie.longtask.LongTaskListener;
import org.mongkie.visualization.MongkieDisplay;
import org.openide.ErrorManager;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class EnrichmentModelImpl extends EnrichmentModel {

    private LongTaskExecutor executor;

    public EnrichmentModelImpl(MongkieDisplay display) {
        super(display);
        executor = new LongTaskExecutor(true, "Enrichment");
        executor.setLongTaskListener(new LongTaskListener() {
            @Override
            public void taskStarted(LongTask task) {
                setRunning(true);
            }

            @Override
            public void taskFinished(LongTask task) {
                setRunning(false);
            }
        });
        executor.setDefaultErrorHandler(new LongTaskErrorHandler() {
            @Override
            public void fatalError(Throwable t) {
                ErrorManager.getDefault().notify(t);
            }
        });
    }

    LongTaskExecutor getExecutor() {
        return executor;
    }

    @Override
    public boolean isRunning() {
        return executor.isRunning();
    }

    @Override
    protected void setRunning(boolean running) {
        if (running) {
            for (EnrichmentModelListener l : listeners) {
                l.analyzingStarted(get());
            }
        } else {
            for (EnrichmentModelListener l : listeners) {
                l.analyzingFinished(get());
            }
        }
    }

    @Override
    protected void changed(Enrichment o, Enrichment n) {
        for (EnrichmentModelListener l : listeners) {
            l.enrichmentChanged(o, n);
        }
    }

    @Override
    protected void load(Enrichment e) {
    }

    @Override
    protected void unload(Enrichment e) {
    }

    @Override
    public String getGeneIDColumn() {
        return geneIdCols.get(get());
    }

    void setGeneIDColumn(String geneIdCol) {
        geneIdCols.put(get(), geneIdCol);
    }
    private final Map<Enrichment, String> geneIdCols = new HashMap<Enrichment, String>();

    @Override
    public EnrichedResultUI getResult() {
        return results.get(get());
    }

    @Override
    public EnrichedResultUI getResult(Enrichment en) {
        return results.get(en);
    }

    void clearResult(Enrichment en) {
        if (results.containsKey(en)) {
            results.remove(en).destroy();
        }
    }

    void setResult(Enrichment en, EnrichedResultUI result) {
        results.put(en, result);
    }
    private final Map<Enrichment, EnrichedResultUI> results = new HashMap<Enrichment, EnrichedResultUI>();
}
