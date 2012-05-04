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
package org.mongkie.enrichment.util;

import java.util.HashMap;
import java.util.Map;
import org.mongkie.enrichment.spi.EnrichedTerm;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class EnrichedTermNode<T extends EnrichedTerm> extends AbstractNode {

    private final T term;

    private EnrichedTermNode(T term) {
        super(Children.LEAF, Lookups.singleton(term));
        this.term = term;
    }

    public T getEnrichedTerm() {
        return term;
    }

    public static <T extends EnrichedTerm> EnrichedTermNode<T> valueOf(T term) {
        return Holder.get(term);

    }

    public static <T extends EnrichedTerm> EnrichedTermNode<T>[] createNodes(T... terms) {
        EnrichedTermNode<T>[] nodes = new EnrichedTermNode[terms.length];
        for (int i = 0; i < terms.length; i++) {
            nodes[i] = EnrichedTermNode.valueOf(terms[i]);
        }
        return nodes;
    }

    private static class Holder {

        private static final Map<EnrichedTerm, EnrichedTermNode> POOL = new HashMap<EnrichedTerm, EnrichedTermNode>();

        private static <T extends EnrichedTerm> EnrichedTermNode<T> get(T term) {
            EnrichedTermNode<T> node = POOL.get(term);
            if (node == null) {
                POOL.put(term, node = new EnrichedTermNode<T>(term));
            }
            return node;
        }
    }
}
