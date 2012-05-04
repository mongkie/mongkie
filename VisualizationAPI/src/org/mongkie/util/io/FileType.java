/*
 * This file is part of MONGKIE. Visit <http://www.mongkie.org/> for details.
 * Copyright (C) 2011 Korean Bioinformation Center(KOBIC)
 * 
 * MONGKIE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your processorUI) any later version.
 * 
 * MONGKIE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.mongkie.util.io;

/**
 * 
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public final class FileType {

    private final String[] extensions;
    private final String name;

    public FileType(String name, String extension) {
        this.name = name;
        this.extensions = new String[]{extension};
    }

    public FileType(String name, String... extensions) {
        this.name = name;
        this.extensions = extensions;
    }

    public String getExtension() {
        return extensions[0];
    }

    public String[] getExtensions() {
        return extensions;
    }

    public String getName() {
        return name;
    }
}
