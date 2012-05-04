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
package org.mongkie.visualmap.impl;

import java.util.*;
import org.mongkie.visualization.MongkieDisplay;
import org.mongkie.visualmap.VisualMapping;
import org.mongkie.visualmap.ranking.Interpolator;
import org.mongkie.visualmap.ranking.RankingEvent;
import org.mongkie.visualmap.ranking.RankingEvent.Type;
import static org.mongkie.visualmap.ranking.RankingEvent.Type.*;
import org.mongkie.visualmap.ranking.RankingModel;
import org.mongkie.visualmap.ranking.RankingModelListener;
import org.mongkie.visualmap.spi.ranking.Ranking;
import org.mongkie.visualmap.spi.ranking.RankingBuilder;
import org.mongkie.visualmap.spi.ranking.Transformer;
import org.mongkie.visualmap.spi.ranking.TransformerBuilder;
import org.openide.util.Lookup;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public final class RankingModelImpl extends RankingModel {

    private Interpolator interpolator;
    private String currentElementType;
    protected boolean rankingListVisible;
    protected final Map<String, Ranking> currentRanking;
    protected final Map<String, Transformer> currentTransformer;
    protected final Map<String, LinkedHashMap<String, Transformer>> transformers;

    RankingModelImpl(MongkieDisplay display) {
        super(display);
        interpolator = Interpolator.LINEAR;
        currentElementType = VisualMapping.NODE_ELEMENT;
        rankingListVisible = false;
        currentRanking = new HashMap<String, Ranking>();
        currentTransformer = new HashMap<String, Transformer>();
        transformers = new HashMap<String, LinkedHashMap<String, Transformer>>();
        initTransformers();
        //Set default transformer - the first
        for (String elementType : RankingControllerImpl.ELEMENT_TYPES) {
            currentTransformer.put(elementType, getTransformers(elementType)[0]);
        }
    }

    private void initTransformers() {
        for (String elementType : RankingControllerImpl.ELEMENT_TYPES) {
            LinkedHashMap<String, Transformer> elmtTransformers = new LinkedHashMap<String, Transformer>();
            transformers.put(elementType, elmtTransformers);
        }
        for (TransformerBuilder builder : Lookup.getDefault().lookupAll(TransformerBuilder.class)) {
            for (String elementType : RankingControllerImpl.ELEMENT_TYPES) {
                Map<String, Transformer> elmtTransformers = transformers.get(elementType);
                if (builder.isTransformerForElement(elementType)) {
                    elmtTransformers.put(builder.getName(), builder.buildTransformer());
                }
            }
        }
    }

    @Override
    public boolean isRankingListVisible() {
        return rankingListVisible;
    }

    void setRankingListVisible(boolean rankingListVisible) {
        if (this.rankingListVisible == rankingListVisible) {
            return;
        }
        boolean old = this.rankingListVisible;
        this.rankingListVisible = rankingListVisible;
        fireRankingEvent(RANKING_LIST_VISIBLE, old, rankingListVisible);
    }

    void setCurrentElementType(String elementType) {
        if (this.currentElementType.equals(elementType)) {
            return;
        }
        String old = this.currentElementType;
        this.currentElementType = elementType;
        fireRankingEvent(CURRENT_ELEMENT_TYPE, old, elementType);
    }

    @Override
    public String getCurrentElementType() {
        return currentElementType;
    }

    void setCurrentTransformer(Transformer transformer) {
        if (currentTransformer.get(currentElementType) == transformer) {
            return;
        }
        Transformer old = currentTransformer.get(currentElementType);
        currentTransformer.put(currentElementType, transformer);
        fireRankingEvent(CURRENT_TRANSFORMER, old, transformer);
    }

    @Override
    public Transformer getCurrentTransformer() {
        return currentTransformer.get(currentElementType);
    }

    @Override
    public Transformer getCurrentTransformer(String elementType) {
        return currentTransformer.get(elementType);
    }

    void setCurrentRanking(Ranking ranking) {
        if ((currentRanking.get(currentElementType) == null && ranking == null)
                || (currentRanking.get(currentElementType) != null && currentRanking.get(currentElementType) == ranking)) {
            return;
        }
        Ranking old = currentRanking.get(currentElementType);
        currentRanking.put(currentElementType, ranking);
        fireRankingEvent(CURRENT_RANKING, old, ranking);
    }

    @Override
    public Ranking getCurrentRanking() {
        return currentRanking.get(currentElementType);
    }

    private void fireRankingEvent(Type eventType, Object oldValue, Object newValue) {
        RankingEvent e = new RankingEventImpl(eventType, this, oldValue, newValue);
        synchronized (listeners) {
            for (Iterator<RankingModelListener> listenerIter = listeners.iterator(); listenerIter.hasNext();) {
                listenerIter.next().processRankingEvent(e);
            }
        }
    }

    void fireRankingEvent(Type eventType, Ranking ranking, Transformer transformer) {
        RankingEvent e = new RankingEventImpl(eventType, this, ranking, transformer);
        synchronized (listeners) {
            for (Iterator<RankingModelListener> listenerIter = listeners.iterator(); listenerIter.hasNext();) {
                listenerIter.next().processRankingEvent(e);
            }
        }
    }

    @Override
    public Ranking[] getNodeRankings() {
        return getRankings(VisualMapping.NODE_ELEMENT);
    }

    @Override
    public Ranking[] getEdgeRankings() {
        return getRankings(VisualMapping.EDGE_ELEMENT);
    }

    @Override
    public Ranking[] getRankings(String elementType) {
        List<Ranking> rankings = new ArrayList<Ranking>();
        Collection<? extends RankingBuilder> builders = Lookup.getDefault().lookupAll(RankingBuilder.class);
        for (RankingBuilder builder : builders) {
            // TODO: caching required??
            Ranking[] builtRankings = builder.buildRankings(this);
            if (builtRankings != null) {
                for (Ranking r : builtRankings) {
                    if (r.getElementType().equals(elementType)) {
                        rankings.add(r);
                    }
                }
            }
        }
        Ranking current = getCurrentRanking();
        if (current != null) {
            //Update selectedRanking with latest version
            for (Ranking r : rankings) {
                if (r.getName().equals(current.getName())) {
                    currentRanking.put(elementType, r);
                    break;
                }
            }
        }
        return rankings.toArray(new Ranking[0]);
    }

    @Override
    public Ranking getRanking(String elementType, String name) {
        Ranking[] rankings = getRankings(elementType);
        for (Ranking r : rankings) {
            if (r.getName().equals(name)) {
                return r;
            }
        }
        return null;
    }

    @Override
    public Transformer[] getTransformers(String elementType) {
        return transformers.get(elementType).values().toArray(new Transformer[0]);
    }

    @Override
    public Transformer getTransformer(String elementType, String name) {
        return transformers.get(elementType).get(name);
    }

    @Override
    public Interpolator getInterpolator() {
        return interpolator;
    }

    void setInterpolator(Interpolator interpolator) {
        if (interpolator == null) {
            throw new NullPointerException();
        }
        this.interpolator = interpolator;
    }

    @Override
    protected void changed(Ranking[] o, Ranking[] n) {
    }

    @Override
    protected void load(Ranking[] data) {
    }

    @Override
    protected void unload(Ranking[] data) {
    }
}
