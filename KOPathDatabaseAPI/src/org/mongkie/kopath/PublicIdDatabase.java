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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.openide.util.Exceptions;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public enum PublicIdDatabase {

    ChEBI("chebi", "http://www.ebi.ac.uk/chebi/searchId.do?chebiId=CHEBI:", "http://www.ebi.ac.uk/chebi"),
    CAS("chemicalabstracts", null, "http://www.cas.org"),
    ENA("embl", "http://www.ebi.ac.uk/ena/data/view/", "http://www.ebi.ac.uk/ena"),
    Entrez("entrez", "http://www.ncbi.nlm.nih.gov/gene?term=", "http://www.ncbi.nlm.nih.gov/gene"),
    ExPASy("enzymeconsortium", "http://enzyme.expasy.org/EC/", "http://enzyme.expasy.org"),
    GO("go", "http://amigo.geneontology.org/cgi-bin/amigo/term_details?term=GO:", "http://amigo.geneontology.org"),
    KEGG("kegg", "http://www.genome.jp/dbget-bin/www_bget?cpd:", "http://www.genome.jp/kegg"),
    PubChem("pubchem", "http://pubchem.ncbi.nlm.nih.gov/summary/summary.cgi?cid=", "http://pubchem.ncbi.nlm.nih.gov"),
    UniProt("uniprot", "http://www.uniprot.org/uniprot/", "http://www.uniprot.org");
    private final String name;
    private final String hyperlinkBase;
    private final String databaseHome;
    public static final String ID_DELIM = ", ";
    public static String URL_DELIM = "+";

    static {
        try {
            URL_DELIM = URLEncoder.encode(" ", "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private PublicIdDatabase(String name, String hyperlinkBase, String databaseHome) {
        this.name = name;
        this.hyperlinkBase = hyperlinkBase;
        this.databaseHome = databaseHome;
    }

    public String getName() {
        return name;
    }

    public static PublicIdDatabase of(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        for (PublicIdDatabase db : values()) {
            if (db.name.equals(name)) {
                return db;
            }
        }
        return null;
    }

    public String getExternalLink(String id) {
        id = id.replaceAll(ID_DELIM, URL_DELIM);
        switch (this) {
            case PubChem:
                id = id.replaceAll("\\bC0*", "");
                if (id.contains(URL_DELIM)) {
                    return "http://www.ncbi.nlm.nih.gov/pccompound?term=" + id;
                }
                break;
            default:
                break;
        }
        return hyperlinkBase != null ? hyperlinkBase + id : null;
    }

    public String getDatabaseLink() {
        return databaseHome;
    }
}
