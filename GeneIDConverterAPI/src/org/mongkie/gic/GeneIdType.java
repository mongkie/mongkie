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
package org.mongkie.gic;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public enum GeneIdType {

    NONE("Arbitrary type", "", "", "", "", false),
    AffyProbeID("Affy Probe ID", ".*_at", "entrezgene2affymetrixu133plus2", "GeneID", "AffymetrixU133", true),
    Entrez("Entrez gene ID", "[0-9]+", "entrezgene2entrezgene", "GeneID", "Entrez_Gene_ID", false),
    Ensembl("Ensembl gene ID", "ENS[A-Z]*G[0-9]{11}", "entrezgene2ensembl", "GeneID", "Ensembl_Gene_ID", false),
    EnsemblTranscript("Ensembl Transcript ID", "ENS[A-Z]*T[0-9]{11}", "entrezgene2ensembltranscript", "GeneID", "Ensembl_Transcript_ID", false),
    GenBankAcc("GenBank Accession", "", "entrezgene2genbankaccession", "GeneID", "GenBank_Accession", true),
    GenBankID("GenBank ID Number", "", "entrezgene2genbankid", "GeneID", "GenBank_ID", true),
    IlluminaProbeID("Illumina Probe ID", "", "entrezgene2illumina", "GeneID", "Illumina_Probe_ID", true),
    PIRAcc("PIR Accession", "", "entrezgene2piraccession_homo_sapiens", "GeneID", "PIR_PSD_Accession", true),
    HGNCID("Official Gene ID by HGNC", "", "entrezgene2hgncid", "GeneID", "HGNC_ID", true),
    HGNCSymbol("Official Gene Symbol by HGNC", "", "entrezgene2hgncsymbol", "GeneID", "HGNC_Symbol", false),
    RefSeqDNA("RefSeq ID", "(N|X)(M|R|G)_[0-9]+", "entrezgene2refseqdna", "GeneID", "RefSeq_DNA_ID", false),
    RefSeqProtein("RefSeq ID", "(N|X)(M|R|G)_[0-9]+", "entrezgene2refseqprotein", "GeneID", "RefSeq_Protein_ID", false),
    TRANSFAC("TRANSFAC ID", "^[A-Z]+[0-9]+$", "entrezgene2transfac", "GeneID", "TRANSFAC_ID", false),
    UniGene("UniGene", "Hs.[0-9]+", "entrezgene2unigene", "GeneID", "UniGene_ID", false),
    UniProtKB("UniProt KB Accession", "", "entrezgene2uniprotkbaccession_homo_sapiens", "GeneID", "UniProtKB_accession", false),
    UniProtID("UniProt KB ID", "^.*(_HUMAN)$", "entrezgene2uniprotkbid_homo_sapiens", "GeneID", "UniProtKB_ID", true);
    private final String name;
    private final String pattern;
    private final String xTableName;
    private final String xColNameX;
    private final String xColNameY;
    private final boolean xUseQuery;

    GeneIdType(String name, String pattern, String xTableName, String xColNameX, String xColNameY, boolean xUseQuery) {
        this.name = name;
        this.pattern = pattern;
        this.xTableName = xTableName;
        this.xColNameX = xColNameX;
        this.xColNameY = xColNameY;
        this.xUseQuery = xUseQuery;
    }

    public String getName() {
        return name;
    }

    public String getPattern() {
        return pattern;
    }

    public String getXTableName() {
        return xTableName;
    }

    public String getXColNameX() {
        return xColNameX;
    }

    public String getXColNameY() {
        return xColNameY;
    }

    public static String getXColNameY(String xTableName) {
        for (GeneIdType type : values()) {
            if (type.getXTableName().equalsIgnoreCase(xTableName)) {
                return type.getXColNameY();
            }
        }
        return null;
    }

    public boolean getXUseQuery() {
        return xUseQuery;
    }
}
