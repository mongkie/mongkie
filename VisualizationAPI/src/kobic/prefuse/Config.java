/*
 * This file is part of MONGKIE. Visit <http://www.mongkie.org/> for details.
 * Copyright (C) 2011 Korean Bioinformation Center(KOBIC)
 *
 * MONGKIE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MONGKE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package kobic.prefuse;

import java.awt.Font;
import prefuse.util.ColorLib;
import prefuse.util.FontLib;
import prefuse.util.PrefuseLib;

/**
 * 
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public interface Config {

    public static final int NODESIZE_BASE = 40;
    public static final int NODESCALE_MIN = 1;
    public static final int NODESCALE_MAX = 10;
    public static final Font FONT_DEFAULT_NODETEXT = FontLib.getFont("SansSerif", 10);
    public static final Font FONT_DEFAULT_EDGETEXT = FontLib.getFont("SansSerif", 8);
    public static final Font FONT_DEFAULT_AGGRTEXT = PrefuseLib.FONT_DEFAULT_AGGRTEXT;
    public static final int COLOR_TRANSPARENT = ColorLib.setAlpha(ColorLib.gray(255), 0);
    public static final int COLOR_DEFAULT_NODE_HOVER = ColorLib.rgb(50, 255, 50);
    public static final int COLOR_DEFAULT_NODE_HIGHLIGHT = ColorLib.rgb(50, 255, 50);
    public static final int COLOR_DEFAULT_NODE_FIXED = ColorLib.rgb(255, 100, 100);
    public static final int COLOR_DEFAULT_ITEM_FOCUS = ColorLib.rgb(255, 200, 0);
    public static final int COLOR_DEFAULT_EDGE_HIGHLIGHT = ColorLib.rgb(50, 255, 50);
    public static final int COLOR_DEFAULT_NODE_STROKE = ColorLib.gray(255);
    public static final int COLOR_DEFAULT_NODE_FILL = ColorLib.rgb(201, 197, 172);
    public static final int COLOR_DEFAULT_NODE_TEXT = ColorLib.gray(50);
    public static final int COLOR_DEFAULT_EDGE_STROKE = ColorLib.gray(230);
    public static final int COLOR_DEFAULT_EDGE_FILL = ColorLib.gray(230);
    public static final int COLOR_DEFAULT_EDGE_TEXT = ColorLib.gray(80);
    public static final int COLOR_DEFAULT_AGGR_STROKE = ColorLib.gray(150);
//    public static final int COLOR_DEFAULT_AGGR_FILL = ColorLib.gray(200, 60);
//    public static final int COLOR_DEFAULT_AGGR_FILL = ColorLib.gray(200, ALPHA_FILLCOLOR);
    public static final int COLOR_DEFAULT_AGGR_FILL = COLOR_DEFAULT_AGGR_STROKE;
//    public static final int COLOR_DEFAULT_AGGR_TEXT = ColorLib.gray(255, 128);
    public static final int COLOR_DEFAULT_AGGR_TEXT = PrefuseLib.COLOR_DEFAULT_AGGR_TEXT;
    public static final int COLOR_DEFAULT_AGGR_HOVER_STROKE = ColorLib.rgb(255, 100, 100);
    public static final int COLOR_AGGRFILL_ALPHA = 60;
}
