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
package kobic.prefuse.data.io;

import java.awt.BasicStroke;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import prefuse.util.StrokeLib;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class SerializableBasicStroke implements Serializable {

    private transient BasicStroke s;

    public SerializableBasicStroke(BasicStroke s) {
        this.s = s;
    }

    public BasicStroke getStroke() {
        return s;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeFloat(s.getLineWidth());
        out.writeInt(s.getEndCap());
        out.writeInt(s.getLineJoin());
        out.writeFloat(s.getMiterLimit());
        out.writeObject(s.getDashArray());
        out.writeFloat(s.getDashPhase());
    }

    private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
        this.s = StrokeLib.getStroke(
                in.readFloat(),
                in.readInt(),
                in.readInt(),
                in.readFloat(),
                (float[]) in.readObject(),
                in.readFloat());
    }
}
