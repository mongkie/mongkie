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
package org.mongkie.visualmap.spi.ranking;

import java.util.HashMap;
import java.util.Map;
import javax.swing.JPanel;
import org.mongkie.visualization.workspace.ModelChangeListener;
import org.mongkie.visualmap.ranking.RankingController;
import org.mongkie.visualmap.ranking.RankingEvent;
import org.mongkie.visualmap.ranking.RankingModel;
import org.mongkie.visualmap.ranking.RankingModelListener;
import org.openide.util.Lookup;
import static org.mongkie.visualmap.ranking.RankingEvent.Type.*;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public abstract class AbstractTransformerUI<P extends JPanel> implements TransformerUI, RankingModelListener {

    private final Map<Transformer, P> panels = new HashMap<Transformer, P>();

    public AbstractTransformerUI() {
        Lookup.getDefault().lookup(RankingController.class).addModelChangeListener(new ModelChangeListener<RankingModel>() {

            @Override
            public void modelChanged(RankingModel o, RankingModel n) {
                if (o != null) {
                    o.removeModelListener(AbstractTransformerUI.this);
                }
                if (n != null) {
                    n.addModelListener(AbstractTransformerUI.this);
                }
            }
        });
    }

    @Override
    public P getPanel(Transformer transformer, Ranking ranking) {
        P p = panels.get(transformer);
        if (p == null) {
            p = buildPanel(transformer, ranking);
            panels.put(transformer, p);
        }
        refresh(p, ranking);
        return p;
    }

    protected abstract P buildPanel(Transformer transformer, Ranking ranking);

    protected abstract void refresh(P panel, Ranking ranking);

    @Override
    public void processRankingEvent(RankingEvent e) {
        if (e.is(APPLY_TRANSFORMER)) {
            P p = panels.get(e.getTransformer());
            if (p != null) {
                transformerApplied(p);
            }
        }
    }

    protected abstract void transformerApplied(P panel);
}
