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
package org.mongkie.vm.ranking.transformer;

import org.mongkie.visualmap.spi.ranking.AbstractSizeTransformer;
import org.mongkie.visualmap.spi.ranking.Ranking;
import org.mongkie.visualmap.spi.ranking.Transformer;
import org.mongkie.visualmap.spi.ranking.TransformerBuilder;
import org.openide.util.lookup.ServiceProvider;
import prefuse.util.PrefuseLib;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;

/**
 * Renderable size transformer builder. Builds <code>ItemSizeTransformer</code>
 * instances, that receives {@link VisualItem} targets. VisualItem can be {@link NodeItem} or
 * {@link EdgeItem}.
 * 
 * @author Mathieu Bastian <mathieu.bastian@gephi.org>
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
@ServiceProvider(service = TransformerBuilder.class, position = 200)
public class ItemSizeTransformerBuilder implements TransformerBuilder {

    @Override
    public Transformer buildTransformer() {
        return new ItemSizeTransformer();
    }

    @Override
    public boolean isTransformerForElement(String elementType) {
        return elementType.equals(Ranking.NODE_ELEMENT) || elementType.equals(Ranking.EDGE_ELEMENT);
    }

    @Override
    public String getName() {
        return Transformer.ITEM_SIZE;
    }

    public static class ItemSizeTransformer extends AbstractSizeTransformer<VisualItem> {

        @Override
        public Object transform(VisualItem target, float normalizedValue) {
            double size = getSize(normalizedValue);
//            target.setSize(size);
            PrefuseLib.update(target, VisualItem.SIZE, size);
            return Double.valueOf(size);
        }
    }
}
