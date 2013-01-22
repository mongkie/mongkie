/*
 * This file is part of MONGKIE. Visit <http://www.mongkie.org/> for details.
 * Visit <http://www.mongkie.org> for details about MONGKIE.
 * Copyright (C) 2013 Korean Bioinformation Center (KOBIC)
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
package org.mongkie.visualization.impl;

import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import kobic.prefuse.data.TupleProvider;
import org.mongkie.visualization.search.SearchController;
import org.mongkie.visualization.search.SearchOption;
import org.mongkie.visualization.search.SearchResult;
import org.openide.util.lookup.ServiceProvider;
import prefuse.data.Schema;
import prefuse.data.Tuple;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
@ServiceProvider(service = SearchController.class)
public class SearchControllerImpl implements SearchController {

    @Override
    public boolean isStringColumnAvailable(Schema s) {
        for (int i = 0; i < s.getColumnCount(); i++) {
            if (s.getColumnType(i) == String.class) {
                return true;
            }
        }
        return false;
    }

    @Override
    public <T extends TupleProvider> SearchResult<T> search(T[] sources, String text, SearchOption options, SearchResult<T> results, String... columns) {
        if (results == null) {
            results = new SearchResult<T>();
        } else {
            assert results.isEmpty();
        }
        Pattern pattern = makeRegexPattern(text, options);
        results.setPattern(pattern);
        for (T data : sources) {
            if (!results.contains(data) && match(data.getTuple(), pattern, columns)) {
                results.add(data);
            }
        }
        return results;
    }

    @Override
    public <T extends TupleProvider> SearchResult<T> search(Object[] sources, String text, SearchOption options, SearchResult<T> results, String... columns) {
        if (results == null) {
            results = new SearchResult<T>();
        } else {
            assert results.isEmpty();
        }
        Pattern pattern = makeRegexPattern(text, options);
        results.setPattern(pattern);
        for (Object source : sources) {
            T data = (T) source;
            if (!results.contains(data) && match(data.getTuple(), pattern, columns)) {
                results.add(data);
            }
        }
        return results;
    }

    @Override
    public <T extends TupleProvider> SearchResult<T> search(Iterator<T> sources, String text, SearchOption options, SearchResult<T> results, String... columns) {
        if (results == null) {
            results = new SearchResult<T>();
        } else {
            assert results.isEmpty();
        }
        Pattern pattern = makeRegexPattern(text, options);
        results.setPattern(pattern);
        while (sources.hasNext()) {
            T data = sources.next();
            if (!results.contains(data) && match(data.getTuple(), pattern, columns)) {
                results.add(data);
            }
        }
        return results;
    }

    @Override
    public Pattern makeRegexPattern(String text, SearchOption options) {
        return makeRegexPattern(text, options.isWholeWords(), options.isCaseSensitive());
    }

    @Override
    public Pattern makeRegexPattern(String text, boolean wholeWords, boolean caseSensitive) {
        String query = wholeWords ? ".*\\b(" + text + ")\\b.*" : ".*(" + text + ").*";
        return caseSensitive ? Pattern.compile(query) : Pattern.compile(query, Pattern.CASE_INSENSITIVE);
    }

    @Override
    public boolean match(Tuple data, Pattern pattern, String... columns) {
        for (String col : columns) {
            String value = data.getString(col);
            if (value != null && !value.isEmpty() && pattern.matcher(value).matches()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public <T extends TupleProvider> T replace(SearchResult<T> results, String replacement, boolean forward, String... columns) {
        replace(results.current(), results.getPattern(), replacement, columns);
        results.remove(forward);
        return results.isEmpty() ? null : results.current();
    }

    @Override
    public <T extends TupleProvider> int replaceAll(SearchResult<T> results, String replacement, String... columns) {
        int occurrences = results.size();
        for (T result : results) {
            replace(result, results.getPattern(), replacement, columns);
        }
        results.clear();
        return occurrences;
    }

    @Override
    public <T extends TupleProvider> void replace(T result, Pattern pattern, String replacement, String... columns) {
        Tuple data = result.getTuple();
        for (String col : columns) {
            String value = data.getString(col);
            Matcher m;
            if (value != null && !value.isEmpty() && (m = pattern.matcher(value)).matches()) {
                data.setString(col, value.replace(m.group(1), replacement));
            }
        }
    }
}
