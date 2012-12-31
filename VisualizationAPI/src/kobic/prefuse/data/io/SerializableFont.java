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

import java.awt.Font;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import prefuse.util.FontLib;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class SerializableFont implements Serializable {

    private transient Font f;

    public SerializableFont(Font f) {
        this.f = f;
    }

    public Font getFont() {
        return f;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeUTF(f.getName());
        out.writeInt(f.getStyle());
        out.writeInt(f.getSize());
    }

    private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
        f = FontLib.getFont(in.readUTF(), in.readInt(), in.readInt());
    }
}
