/*
 *  This file is part of MONGKIE. Visit <http://www.mongkie.org/> for details.
 *  Copyright (C) 2012 Korean Bioinformation Center(KOBIC)
 * 
 *  MONGKIE is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  MONGKE is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 * 
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package kobic.prefuse;

import java.awt.Polygon;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public enum EdgeArrow {

    NONE(0, "None", null),
    ARROW(1, "Arrow", new Polygon(new int[]{0, -5, 0, 5}, new int[]{0, -14, -10, -14}, 4), 10),
    T(2, "Vertical Bar", new Polygon(new int[]{-7, -7, 7, 7}, new int[]{-2, -6, -6, -2}, 4), 6),
    RECTANGLE(3, "Rectangle", new Polygon(new int[]{-5, -5, 5, 5}, new int[]{-2, -12, -12, -2}, 4), 12),
    DIAMOND(4, "Diamond", new Polygon(new int[]{0, -5, 0, 5}, new int[]{0, -7, -14, -7}, 4), 12);
    private final int code;
    private final String name;
    private final int gap;
    private final Polygon head;

    private EdgeArrow(int code, String name, Polygon head) {
        this(code, name, head, head == null ? 0 : head.getBounds().height);
    }

    private EdgeArrow(int code, String name, Polygon head, int gap) {
        this.code = code;
        this.name = name;
        this.head = head;
        this.gap = gap;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public int getGap() {
        return gap;
    }

    public Polygon getArrowHead() {
        return head;
    }

    public static EdgeArrow get(int code) {
        return codes.get(code);
    }
    private static final Map<Integer, EdgeArrow> codes = new HashMap<Integer, EdgeArrow>();

    static {
        for (EdgeArrow a : values()) {
            codes.put(a.getCode(), a);
        }
    }

    public static EdgeArrow get(String name) {
        return names.get(name);
    }
    private static final Map<String, EdgeArrow> names = new HashMap<String, EdgeArrow>();

    static {
        for (EdgeArrow a : values()) {
            names.put(a.getName(), a);
        }
    }

    @Override
    public String toString() {
        return getName();
    }
}
