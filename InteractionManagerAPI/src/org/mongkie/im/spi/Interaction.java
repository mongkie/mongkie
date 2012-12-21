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
package org.mongkie.im.spi;

import java.util.List;
import kobic.prefuse.data.Attribute;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public interface Interaction<K> {

    public K getSourceKey();

    public K getTargetKey();
    
    public boolean isDirected();

    public Interactor<K> getInteractor();

    public Attribute.Set getAttributeSet();

    public static class Interactor<K> {

        private final K key;
        private final Attribute.Set attributes;
        private final boolean hasAttributes;

        public Interactor(K key) {
            this(key, null);
        }

        public Interactor(K key, Attribute.Set attributes) {
            this.key = key;
            this.attributes = attributes;
            hasAttributes = attributes != null && !attributes.isEmpty();
        }

        public boolean hasAttributes() {
            return hasAttributes;
        }

        public K getKey() {
            return key;
        }

        public Attribute.Set getAttributeSet() {
            return attributes;
        }

        public List<Attribute> getAttributes() {
            return attributes.getList();
        }

        public Object getAttribute(String name) {
            return attributes.getAttribute(name);
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 17 * hash + (this.key != null ? this.key.hashCode() : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Interactor<K> other = (Interactor<K>) obj;
            if (this.key != other.key && (this.key == null || !this.key.equals(other.key))) {
                return false;
            }
            return true;
        }
    }
}
