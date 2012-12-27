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

import java.util.Map;
import java.util.Set;
import javax.swing.JPanel;
import kobic.prefuse.data.Attribute;
import kobic.prefuse.data.Schema;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public interface InteractionSource<K> {

    public String getName();

    public String getDescription();

    public String getCategory();

    public Schema getInteractionSchema();

    public Schema getAnnotationSchema();

    public boolean isDirected();

    public <I extends Interaction<K>> Map<K, Set<I>> query(K... keys) throws Exception;

    public Map<K, Attribute.Set> annotate(K... keys) throws Exception;

    public Class<K> getKeyType();

    public SettingUI getSettingUI();

    public static interface SettingUI<I extends InteractionSource> {

        public JPanel getPanel();

        public void load(I is);

        public void apply(I is);
    }
}
