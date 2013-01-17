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
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import kobic.prefuse.data.io.ReaderFactory;
import kobic.prefuse.display.DisplayListener;
import kobic.prefuse.display.NetworkDisplay;
import org.mongkie.longtask.LongTask;
import org.mongkie.longtask.progress.Progress;
import org.mongkie.longtask.progress.ProgressTicket;
import org.mongkie.series.*;
import org.mongkie.visualization.MongkieDisplay;
import org.mongkie.visualization.VisualizationController;
import org.mongkie.visualization.workspace.WorkspaceListener;
import org.openide.ErrorManager;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import prefuse.data.Graph;
import prefuse.data.Table;
import prefuse.data.Tuple;
import prefuse.data.io.CSVTableReader;
import prefuse.data.io.DataIOException;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
@ServiceProvider(service = SeriesController.class)
public class SeriesControllerImpl implements SeriesController, DisplayListener {

    private SeriesModelImpl model;
    private List<SeriesModelChangeListener> listeners;

    public SeriesControllerImpl() {
        listeners = new ArrayList<SeriesModelChangeListener>();
        Lookup.getDefault().lookup(VisualizationController.class).addWorkspaceListener(new WorkspaceListener() {
            @Override
            public void displaySelected(MongkieDisplay display) {
                display.addDisplayListener(SeriesControllerImpl.this);
                SeriesModelImpl old = model;
                model = display.getLookup().lookup(SeriesModelImpl.class);
                if (model == null) {
                    model = new SeriesModelImpl(display);
                    display.add(model);
                }
                fireModelChangeEvent(old, model);
            }

            @Override
            public void displayDeselected(MongkieDisplay display) {
                display.removeDisplayListener(SeriesControllerImpl.this);
            }

            @Override
            public void displayClosed(MongkieDisplay display) {
            }

            @Override
            public void displayClosedAll() {
                fireModelChangeEvent(model, null);
            }
        });
        MongkieDisplay d = Lookup.getDefault().lookup(VisualizationController.class).getDisplay();
        if (d != null) {
            model = d.getLookup().lookup(SeriesModelImpl.class);
            if (model == null) {
                model = new SeriesModelImpl(d);
                d.add(model);
            }
        }
    }

    @Override
    public SeriesModel getModel() {
        return model;
    }

    @Override
    public void loadSeries(SeriesImporter importer) {
        SeriesLoader loader = new SeriesLoader(model, importer);
        model.getExecutor().execute(loader, loader);
    }

    @Override
    public void clearSeries() {
        model.clearData();
    }

    @Override
    public void addModelChangeListener(SeriesModelChangeListener l) {
        if (!listeners.contains(l)) {
            listeners.add(l);
        }
    }

    @Override
    public void removeModelChangeListener(SeriesModelChangeListener l) {
        listeners.remove(l);
    }

    private void fireModelChangeEvent(SeriesModel oldModel, SeriesModel newModel) {
        for (SeriesModelChangeListener l : listeners) {
            l.modelChanged(oldModel, newModel);
        }
    }

    @Override
    public void graphDisposing(NetworkDisplay d, Graph g) {
    }

    @Override
    public void graphChanged(NetworkDisplay d, Graph g) {
        if (g.getNodeCount() > 0) {
            fireModelChangeEvent(null, model);
        } else {
            fireModelChangeEvent(model, null);
        }
    }

    private static class SeriesLoader implements LongTask, Runnable {

        private ProgressTicket progressTicket;
        private final MongkieDisplay display;
        private final SeriesImporter importer;
        private final SeriesModelImpl model;
        private static final CSVTableReader CSVReader = ReaderFactory.createCSVTableReader();

        public SeriesLoader(SeriesModelImpl model, SeriesImporter importer) {
            this.importer = importer;
            this.model = model;
            this.display = model.getDisplay();
            CSVReader.setHasHeader(importer.hasHeaderRecord());
        }

        @Override
        public boolean cancel() {
            return false;
        }

        @Override
        public void setProgressTicket(ProgressTicket progressTicket) {
            this.progressTicket = progressTicket;
        }

        @Override
        public void run() {
            Progress.setDisplayName(progressTicket, "Loading expression profile");
            Progress.start(progressTicket);
            // TODO: series is double? String?
            try {
                final Table seriesTable = CSVReader.readTable(importer.getInputStream());
                if (seriesTable != null) {
                    final Table nodeTable = display.getGraph().getNodeTable();
                    String seriesKey = seriesTable.getColumnName(0);
                    seriesTable.index(seriesKey);
                    display.getVisualization().process(new Runnable() {
                        @Override
                        public void run() {
                            for (int i = 1; i < seriesTable.getColumnCount(); i++) {
                                String colName = seriesTable.getColumnName(i);
                                if (nodeTable.getColumn(colName) == null) {
                                    nodeTable.addColumn(colName, double.class);
                                }
                            }
                        }
                    });
                    double[][] matrix = new double[seriesTable.getColumnCount() - 1][nodeTable.getTupleCount()];
                    int j = 0;
                    Iterator<Tuple> nodesIter = nodeTable.tuples();
                    while (nodesIter.hasNext()) {
                        Tuple node = nodesIter.next();
                        int row = seriesTable.getIndex(seriesKey).get(node.getString(importer.getKeyField()));
                        Tuple s = row < 0 ? null : seriesTable.getTuple(row);
                        if (s != null) {
                            for (int i = 1; i < seriesTable.getColumnCount(); i++) {
                                try {
                                    double val = Double.parseDouble(s.getString(i));
                                    node.set(s.getColumnName(i), val);
                                    matrix[i - 1][j] = val;
                                } catch (NumberFormatException ex) {
                                    continue;
                                }
                            }
                        }
                        j++;
                    }
                    model.setData(new SeriesData(importer.getTitle(), matrix));
                }
            } catch (DataIOException ex) {
                Logger.getLogger(SeriesControllerImpl.class.getName()).log(Level.SEVERE, null, ex);
                ErrorManager.getDefault().notify(ex);
            } finally {
                Progress.finish(progressTicket);
            }
        }
    }
}
