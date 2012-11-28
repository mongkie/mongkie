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

import java.awt.Color;
import org.mongkie.visualization.color.ColorController;
import org.mongkie.visualmap.spi.ranking.AbstractColorTransformer;
import org.mongkie.visualmap.spi.ranking.Ranking;
import org.mongkie.visualmap.spi.ranking.Transformer;
import org.mongkie.visualmap.spi.ranking.TransformerBuilder;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import prefuse.util.ColorLib;
import prefuse.util.PrefuseLib;
import prefuse.visual.VisualItem;

/**
 * Label color transformer builder. Builds <code>LabelColorTransformer</code>
 * instances, that receives {@link VisualItem} targets.
 * 
 * @author Mathieu Bastian <mathieu.bastian@gephi.org>
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
@ServiceProvider(service = TransformerBuilder.class, position = 300)
public class LabelColorTransformerBuilder implements TransformerBuilder {

    @Override
    public Transformer buildTransformer() {
        return new LabelColorTransformer();
    }

    @Override
    public boolean isTransformerForElement(String elementType) {
        return elementType.equals(Ranking.NODE_ELEMENT) || elementType.equals(Ranking.EDGE_ELEMENT);
    }

    @Override
    public String getName() {
        return Transformer.LABEL_COLOR;
    }

    public class LabelColorTransformer extends AbstractColorTransformer<VisualItem> {

        @Override
        public Object transform(VisualItem target, float normalizedValue) {
            Color color = getColor(normalizedValue);
//            PrefuseLib.update(target, VisualItem.TEXTCOLOR,
//                    ColorLib.color(new Color(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, 1f)));
             Lookup.getDefault().lookup(ColorController.class).setTextColor(target, color);
            return color;
        }
    }
}
