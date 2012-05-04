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
package org.mongkie.ui.vm.ranking.transformer;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.mongkie.visualmap.spi.ranking.Ranking;
import org.mongkie.visualmap.spi.ranking.Transformer;
import org.mongkie.visualmap.spi.ranking.AbstractTransformerUI;
import org.mongkie.visualmap.spi.ranking.TransformerUI;
import org.mongkie.vm.ranking.transformer.ItemColorTransformerBuilder.ItemColorTransformer;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
@ServiceProvider(service = TransformerUI.class, position = 100)
public class ItemColorTransformerUI extends AbstractTransformerUI<ColorTransformerPanel> {

    @Override
    public Icon getIcon() {
        return new ImageIcon(getClass().getResource("/org/mongkie/ui/vm/resources/color.png"));
    }

    @Override
    public boolean isUIForTransformer(Transformer transformer) {
        return transformer instanceof ItemColorTransformer;
    }

    @Override
    protected ColorTransformerPanel buildPanel(Transformer transformer, Ranking ranking) {
        return new ColorTransformerPanel(transformer, ranking);
    }

    @Override
    public String getDisplayName() {
        return "Color";
    }

    @Override
    protected void refresh(ColorTransformerPanel panel, Ranking ranking) {
        panel.refresh(ranking);
    }

    @Override
    protected void transformerApplied(ColorTransformerPanel panel) {
        panel.addRecentPalette();
    }
}
