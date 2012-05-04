/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.mongkie.visualmap.impl;

import org.mongkie.visualmap.spi.ranking.Ranking;
import org.mongkie.visualmap.ranking.RankingEvent;
import org.mongkie.visualmap.ranking.RankingModel;
import org.mongkie.visualmap.spi.ranking.Transformer;

/**
 * Implementation of the <code>RankingEvent</code> interface.
 * 
 * @author Mathieu Bastian
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
class RankingEventImpl implements RankingEvent {

    private final RankingEvent.Type eventType;
    private final RankingModel source;
    private final Ranking ranking;
    private final Transformer transformer;
    private final Object oldValue, newValue;

    RankingEventImpl(Type eventType, RankingModel source, Ranking ranking, Transformer transformer) {
        this(eventType, source, ranking, transformer, null, null);
    }

    RankingEventImpl(Type eventType, RankingModel source, Object oldValue, Object newValue) {
        this(eventType, source, null, null, oldValue, newValue);
    }

    RankingEventImpl(Type eventType, RankingModel source, Ranking ranking, Transformer transformer, Object oldValue, Object newValue) {
        this.eventType = eventType;
        this.source = source;
        this.ranking = ranking;
        this.transformer = transformer;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    RankingEventImpl(Type eventType, RankingModel source) {
        this(eventType, source, null, null, null, null);
    }

    @Override
    public Type getType() {
        return eventType;
    }

    @Override
    public RankingModel getSource() {
        return source;
    }

    @Override
    public Ranking getRanking() {
        return ranking;
    }

    @Override
    public Transformer getTransformer() {
        return transformer;
    }

    @Override
    public Object getNewValue() {
        return newValue;
    }

    @Override
    public Object getOldValue() {
        return oldValue;
    }

    @Override
    public boolean is(Type... type) {
        for (Type e : type) {
            if (e.equals(eventType)) {
                return true;
            }
        }
        return false;
    }
}
