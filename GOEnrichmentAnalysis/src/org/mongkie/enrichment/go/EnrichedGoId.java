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
package org.mongkie.enrichment.go;

import gobean.GoId;
import java.awt.Color;
import org.mongkie.enrichment.spi.EnrichedTerm;
import org.mongkie.gobean.EnrichedResult;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class EnrichedGoId implements EnrichedTerm {

    private final GoId goId;
    private final EnrichedResult result;

    public EnrichedGoId(GoId goId, EnrichedResult result) {
        this.goId = goId;
        this.result = result;
    }

    public GoId getGoId() {
        return goId;
    }

    @Override
    public String getID() {
        return goId.toString();
    }

    @Override
    public String getName() {
        return result.getNameMap().get(goId);
    }

    @Override
    public String getDescription() {
        return result.getDescriptionMap().get(goId);
    }

    @Override
    public String[] getEnrichedGeneIds() {
        return result.getFullStudyMap().get(goId);
//        return result.getCurrentStudyMap().get(goId);
    }

    @Override
    public double getP() {
        return result.getAdjustedP().get(goId);
    }

    @Override
    public String toString() {
        return getID();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final EnrichedGoId other = (EnrichedGoId) obj;
        if (this.goId != other.goId && (this.goId == null || !this.goId.equals(other.goId))) {
            return false;
        }
        if (this.result != other.result && (this.result == null || !this.result.equals(other.result))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (this.goId != null ? this.goId.hashCode() : 0);
        hash = 89 * hash + (this.result != null ? this.result.hashCode() : 0);
        return hash;
    }

    @Override
    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }
    private Color color = null;
}
