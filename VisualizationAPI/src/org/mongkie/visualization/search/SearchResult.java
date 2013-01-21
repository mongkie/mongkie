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
package org.mongkie.visualization.search;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class SearchResult<T> implements Iterable<T> {

    private final List<T> results = new ArrayList<T>();
    private final Set<T> set = new HashSet<T>();
    private int current = 0;
    private Pattern pattern;

    public Pattern getPattern() {
        return pattern;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    public boolean isEmpty() {
        return results.isEmpty();
    }

    public void add(T n) {
        if (set.add(n)) {
            results.add(n);
        }
    }

    public void remove(boolean forward) {
        assert !results.isEmpty();
        T n = results.get(current);
        assert set.remove(n);
        assert results.remove(n);
        if (results.isEmpty() || (forward && current >= results.size())) {
            current = 0;
        } else if (!forward && --current < 0) {
            current = results.size() - 1;
        }
    }

    public boolean contains(T n) {
        return set.contains(n);
    }

    public void clear() {
        results.clear();
        set.clear();
        current = 0;
    }

    public T current() {
        assert !results.isEmpty();
        return results.get(current);
    }

    public T next() {
        assert !results.isEmpty();
        if (++current >= results.size()) {
            current = 0;
        }
        return results.get(current);
    }

    public T previous() {
        assert !results.isEmpty();
        if (--current < 0) {
            current = results.size() - 1;
        }
        return results.get(current);
    }

    @Override
    public Iterator<T> iterator() {
        return results.iterator();
    }

    public int size() {
        return results.size();
    }
}
