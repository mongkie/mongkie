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
package kobic.prefuse.data.io;

import java.util.List;
import java.util.logging.Logger;
import prefuse.data.io.CSVTableReader;
import prefuse.data.io.GraphMLReader;
import prefuse.data.parser.BooleanParser;
import prefuse.data.parser.ColorIntParser;
import prefuse.data.parser.DataParser;
import prefuse.data.parser.DateParser;
import prefuse.data.parser.DateTimeParser;
import prefuse.data.parser.DoubleParser;
import prefuse.data.parser.FloatParser;
import prefuse.data.parser.IntParser;
import prefuse.data.parser.LongParser;
import prefuse.data.parser.ParserFactory;
import prefuse.data.parser.StringParser;
import prefuse.data.parser.TimeParser;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class ReaderFactory {

    private static final GraphMLReader DEFAULT_GraphML_READER = new GraphMLReader();
    private static final CSVTableReader DEFAULT_CSV_TABLE_READER = new CSVTableReaderWithoutArrayParser() {

        @Override
        public void setHasHeader(boolean hasHeaderRow) {
            Logger.getLogger(ReaderFactory.class.getName()).warning("Can not change the behavior of the default reader.");
        }

        @Override
        public void setHeaderNames(List<String> headerNames) {
            Logger.getLogger(ReaderFactory.class.getName()).warning("Can not change the behavior of the default reader.");
        }
    };

    public static GraphMLReader getDefaultGraphMLReader() {
        return DEFAULT_GraphML_READER;
    }

    public static CSVTableReader getDefaultCSVTableReader() {
        return DEFAULT_CSV_TABLE_READER;
    }

    public static CSVTableReader createCSVTableReader() {
        return new CSVTableReaderWithoutArrayParser();
    }

    private static class CSVTableReaderWithoutArrayParser extends CSVTableReader {

        CSVTableReaderWithoutArrayParser() {
            super(new ParserFactory(new DataParser[]{
                        new IntParser(),
                        new LongParser(),
                        new DoubleParser(),
                        new FloatParser(),
                        new BooleanParser(),
                        new ColorIntParser(),
                        new DateParser(),
                        new TimeParser(),
                        new DateTimeParser(),
                        new StringParser()
                    }));
        }
    }
}
