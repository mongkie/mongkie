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
package org.mongkie.kopath;

/**
 * 
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public enum EntityType {

    protein(NodeType.MOLECULE), gene(NodeType.MOLECULE), dna(NodeType.MOLECULE), rna(NodeType.MOLECULE),
    compound(NodeType.COMPOUND), smallMolecule(NodeType.COMPOUND),
    biologicalProcess(NodeType.BIOLOGICAL_PROCESS),
    enzyme(NodeType.ENZYME),
    complex(NodeType.COMPLEX), group(NodeType.COMPLEX),
    superNode(NodeType.SUPEREX);
    private final NodeType nodeType;

    private EntityType(NodeType nodeType) {
        this.nodeType = nodeType;
    }

    public NodeType getNodeType() {
        return nodeType;
    }
}
