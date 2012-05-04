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

import java.awt.Font;
import org.mongkie.visualmap.spi.ranking.AbstractSizeTransformer;
import org.mongkie.visualmap.spi.ranking.Ranking;
import org.mongkie.visualmap.spi.ranking.Transformer;
import org.mongkie.visualmap.spi.ranking.TransformerBuilder;
import org.openide.util.lookup.ServiceProvider;
import prefuse.util.PrefuseLib;
import prefuse.visual.VisualItem;

/**
 * Label size transformer builder. Builds <code>LabelSizeTransformer</code>
 * instances, that receives {@link VisualItem} targets.
 * 
 * @author Mathieu Bastian <mathieu.bastian@gephi.org>
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
@ServiceProvider(service = TransformerBuilder.class, position = 400)
public class LabelSizeTransformerBuilder implements TransformerBuilder {

    @Override
    public Transformer buildTransformer() {
        return new LabelSizeTransformer();
    }

    @Override
    public boolean isTransformerForElement(String elementType) {
        return elementType.equals(Ranking.NODE_ELEMENT) || elementType.equals(Ranking.EDGE_ELEMENT);
    }

    @Override
    public String getName() {
        return Transformer.LABEL_SIZE;
    }

    public static class LabelSizeTransformer extends AbstractSizeTransformer<VisualItem> {

        @Override
        public Object transform(VisualItem target, float normalizedValue) {
            Font font = target.getFont().deriveFont(getSize(normalizedValue) * 12);
            PrefuseLib.update(target, VisualItem.FONT, font);
            return font;
        }
    }
}
