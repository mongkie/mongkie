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
package org.mongkie.visualization;

/**
 *
 * @author yjjang
 */
public abstract class Config implements kobic.prefuse.Config {

    public static final String MODE_ACTION = "explorer";
    public static final String MODE_CONTROL = "navigator";
    public static final String MODE_DISPLAY = "editor";
    public static final String MODE_DATATABLE = "output";
    public static final String MODE_CONTEXT = "properties";
    public static final String MODE_OVERVIEW = "overview";
    public static final String ROLE_NETWORK = "NetworkRole";
}
