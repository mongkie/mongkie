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
package org.mongkie.exporter.impl;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import org.mongkie.exporter.ExportController;
import org.mongkie.exporter.spi.Exporter;
import org.mongkie.exporter.spi.Exporter.OptionUI;
import org.mongkie.exporter.spi.ExporterBuilder;
import org.mongkie.exporter.spi.FileExporter;
import org.mongkie.visualization.VisualizationController;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
@ServiceProvider(service = ExportController.class)
public class ExportControllerImpl implements ExportController {

    @Override
    public OptionUI getOptionUI(Exporter exporter) {
        for (OptionUI ui : Lookup.getDefault().lookupAll(OptionUI.class)) {
            if (ui.isUIForExporter(exporter)) {
                return ui;
            }
        }
        return null;
    }

    @Override
    public <E extends Exporter> ExporterBuilder<E> getBuilder(E exporter) {
        return ExporterPool.getBuilder(exporter);
    }

    @Override
    public <E extends Exporter> E getExporter(ExporterBuilder<E> builder) {
        return ExporterPool.getExporter(builder);
    }

    @Override
    public void exportFile(File file, FileExporter exporter) throws IOException {
        exporter.setDisplay(Lookup.getDefault().lookup(VisualizationController.class).getDisplay());

        OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
        try {
            exporter.setOutputStream(out);
            exporter.execute();
        } catch (Exception ex) {
            out.flush();
            out.close();
            if (ex instanceof RuntimeException) {
                ex.printStackTrace();
                throw (RuntimeException) ex;
            }
            throw new RuntimeException(ex);
        }
        out.flush();
        out.close();
    }

    private static class ExporterPool {

        private static final Map<ExporterBuilder, Exporter> exporters = new HashMap<ExporterBuilder, Exporter>();
        private static final Map<Exporter, ExporterBuilder> builders = new HashMap<Exporter, ExporterBuilder>();

        static {
//            final Lookup.Result<ExporterBuilder> result = Lookup.getDefault().lookupResult(ExporterBuilder.class);
//            result.addLookupListener(new LookupListener() {
//
//                @Override
//                public void resultChanged(LookupEvent ev) {
//                    lookupResultChanged();
//                }
//            });
            lookupResultChanged();
        }

        private static void lookupResultChanged() {
            for (ExporterBuilder builder : Lookup.getDefault().lookupAll(ExporterBuilder.class)) {
                if (exporters.containsKey(builder)) {
                    continue;
                }
                Exporter exporter = builder.buildExporter();
                exporters.put(builder, exporter);
                builders.put(exporter, builder);
            }
        }

        private static <I extends Exporter> I getExporter(ExporterBuilder<I> builder) {
            if (!exporters.containsKey(builder)) {
                lookupResultChanged();
            }
            return (I) exporters.get(builder);
        }

        private static <I extends Exporter> ExporterBuilder<I> getBuilder(I exporter) {
            if (!builders.containsKey(exporter)) {
                lookupResultChanged();
            }
            return builders.get(exporter);
        }
    }
}
