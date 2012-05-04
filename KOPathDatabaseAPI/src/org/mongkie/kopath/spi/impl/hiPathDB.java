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
package org.mongkie.kopath.spi.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mongkie.kopath.Pathway;
import org.mongkie.kopath.rest.PathwayService;
import org.mongkie.kopath.spi.PathwayDatabase;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public abstract class hiPathDB implements PathwayDatabase {

    @Override
    public int getSize() {
        try {
            return PathwayService.countPathway(getCode());
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
        return 0;
    }

    @Override
    public List<Pathway> getPathways() {
        try {
            return PathwayService.searchPathway(getCode());
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
        return new ArrayList<Pathway>();
    }

    @Override
    public int countPathway(String... genes) {
        try {
            return PathwayService.countPathway(getCode(), genes);
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
        return 0;
    }

    @Override
    public List<Pathway> searchPathway(String... genes) {
        try {
            return PathwayService.searchPathway(getCode(), genes);
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
        return new ArrayList<Pathway>();
    }

    @Override
    public int countPathway(String pathway, boolean like) {
        try {
            return PathwayService.countPathway(getCode(), pathway, like);
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
        return 0;
    }

    @Override
    public List<Pathway> searchPathway(String pathway, boolean like) {
        try {
            return PathwayService.searchPathway(getCode(), pathway, like);
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
        return new ArrayList<Pathway>();
    }
}
