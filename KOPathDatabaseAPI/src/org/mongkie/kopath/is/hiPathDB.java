/*
 * This file is part of MONGKIE. Visit <http://www.mongkie.org/> for details.
 * Visit <http://www.mongkie.org> for details about MONGKIE.
 * Copyright (C) 2012 Korean Bioinformation Center (KOBIC)
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
package org.mongkie.kopath.is;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import kobic.prefuse.data.Attribute;
import kobic.prefuse.data.Schema;
import org.mongkie.im.spi.Interaction;
import org.mongkie.im.spi.InteractionAction;
import org.mongkie.im.spi.InteractionSource;
import org.openide.util.lookup.ServiceProvider;
import prefuse.data.Edge;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
//@ServiceProvider(service = InteractionSource.class, position = 2)
public class hiPathDB implements InteractionSource<Integer> {

    @Override
    public String getName() {
        return "hiPathDB";
    }

    @Override
    public String getDescription() {
        return "hiPathDB Binary Interactions";
    }

    @Override
    public String getCategory() {
        return "Pathway";
    }

    @Override
    public Schema getInteractionSchema() {
        return new Schema(new String[]{}, new Class[]{}, new Object[]{});
    }

    @Override
    public Schema getAnnotationSchema() {
        return new Schema(new String[]{}, new Class[]{}, new Object[]{});
    }

    @Override
    public boolean isDirected() {
        return false;
    }

    @Override
    public Map<Integer, Set<BinaryInteraction>> query(Integer... keys) throws Exception {
        Map<Integer, Set<BinaryInteraction>> results = new HashMap<Integer, Set<BinaryInteraction>>();
        return results;
    }

    @Override
    public Map<Integer, Attribute.Set> annotate(Integer... keys) throws Exception {
        Map<Integer, Attribute.Set> results = new HashMap<Integer, Attribute.Set>();
        return results;
    }

    @Override
    public Class<Integer> getKeyType() {
        return Integer.class;
    }

    @Override
    public SettingUI getSettingUI() {
        return null;
    }

    @Override
    public InteractionAction[] getActions() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public static class BinaryInteraction implements Interaction<Integer> {

        @Override
        public Integer getSourceKey() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Integer getTargetKey() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isDirected() {
            return true;
        }

        @Override
        public Interactor<Integer> getInteractor() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Attribute.Set getAttributeSet() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public InteractionSource<Integer> getInteractionSource() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean identicalWith(Edge e) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
    }
    
}
