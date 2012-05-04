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
package org.mongkie.kopath;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import prefuse.util.ColorLib;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public enum ControlType {

    ACTIVATION("activation", ColorLib.setAlpha(ColorLib.color(Color.blue), 220)),
    INHIBITION("inhibition", ColorLib.setAlpha(ColorLib.color(Color.red), 220)),
    EXPRESSION("expression"),
    REPRESSION("depression"),
    INDIRECT_EFFECT("indirect effect"),
    STATE_CHANGE("state change"),
    BINDING_OR_ASSOCIATION("binding/association"),
    DISSOCIATION("dissociation"),
    MISSING_INTERACTION("missing interaction"),
    PHOSPHORYLATION(Category.MOLECULAR_EVENT, "phosphorylation", "+p"),
    DEPHOSPHORYLATION(Category.MOLECULAR_EVENT, "dephosphorylation", "-p"),
    GLYCOSYLATION(Category.MOLECULAR_EVENT, "glycosylation", "+g"),
    UBIQUITINATION(Category.MOLECULAR_EVENT, "ubiquitination", "+u"),
    METHYLATION(Category.MOLECULAR_EVENT, "methylation", "+m");
    private final Category category;
    private final String name;
    private final String symbol;
    private final int color;
    private static final Map<String, ControlType> typesByName = new HashMap<String, ControlType>();

    static {
        for (ControlType type : values()) {
            typesByName.put(type.getName().toUpperCase(), type);
        }
    }
    private static final List<ControlType> controls = new ArrayList<ControlType>();
    private static final List<ControlType> molecularEvents = new ArrayList<ControlType>();

    static {
        for (ControlType type : values()) {
            switch (type.category) {
                case CONTROL:
                    controls.add(type);
                    break;
                case MOLECULAR_EVENT:
                    molecularEvents.add(type);
                    break;
                default:
                    break;
            }
        }
    }
    private static final Map<String, ControlType> typesBySymbol = new HashMap<String, ControlType>();

    static {
        for (ControlType type : values()) {
            String symbol = type.getSymbol();
            if (symbol != null) {
                typesBySymbol.put(symbol, type);
            }
        }
    }

    private ControlType(String name) {
        this(name, ColorLib.gray(140));
    }

    private ControlType(String name, int color) {
        this(Category.CONTROL, name, null, color);
    }

    private ControlType(Category category, String name, String symbol) {
        this(category, name, symbol, ColorLib.gray(0));
    }

    private ControlType(Category category, String name, String symbol, int color) {
        this.category = category;
        this.name = name;
        this.symbol = symbol;
        this.color = color;
    }

    public int getColor() {
        return color;
    }

    public String getName() {
        return name;
    }

    public String getSymbol() {
        return symbol;
    }

    @Override
    public String toString() {
        return getName();
    }

    public static ControlType fromName(String name) {
        return typesByName.get(name.toUpperCase());
    }

    public static ControlType fromSymbol(String symbol) {
        return typesBySymbol.get(symbol);
    }

    public static List<ControlType> getControls() {
        return controls;
    }

    public static List<ControlType> getMolecularEvents() {
        return molecularEvents;
    }

    public static boolean isControl(String controlTypeName) {
        ControlType type = fromName(controlTypeName);
        return type != null && type.category == Category.CONTROL;
    }

    public static boolean isMolecularEvent(String controlTypeName) {
        ControlType type = fromName(controlTypeName);
        return type != null && type.category == Category.MOLECULAR_EVENT;
    }

    private static enum Category {

        CONTROL, MOLECULAR_EVENT
    }
}
