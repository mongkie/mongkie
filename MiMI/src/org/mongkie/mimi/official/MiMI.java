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
package org.mongkie.mimi.official;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import kobic.prefuse.data.Attribute;
import kobic.prefuse.data.Schema;
import org.mongkie.im.spi.Interaction;
import org.mongkie.im.spi.Interaction.Interactor;
import org.mongkie.im.spi.InteractionSource;
import org.mongkie.mimi.jaxb.official.InteractingGene;
import org.mongkie.mimi.jaxb.official.InteractionAttribute;
import org.mongkie.mimi.jaxb.official.NCIBI;
import org.mongkie.mimi.jaxb.official.Result;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
//@ServiceProvider(service = InteractionSource.class, position = 10)
public class MiMI implements InteractionSource<Integer> {

    private final XPath xpath = XPathFactory.newInstance().newXPath();
    private final Schema interactionSchema = InteractionElement.createSchema();
    private final Schema annotationSchema = AnnotationElement.createSchema();

    @Override
    public String getName() {
        return "MiMI";
    }

    @Override
    public String getDescription() {
        return "NCIBI-MiMI Webservice API for querying gene interactions";
    }

    @Override
    public String getCategory() {
        return "PPI";
    }

    @Override
    public Schema getInteractionSchema() {
        return interactionSchema;
    }

    @Override
    public Schema getAnnotationSchema() {
        return annotationSchema;
    }

    @Override
    public Map<Integer, Set<PPI>> query(Integer... geneIds) throws JAXBException, MalformedURLException {
        Map<Integer, Set<PPI>> results = new HashMap<Integer, Set<PPI>>();
        for (Integer geneId : geneIds) {
            results.put(geneId, fetch(geneId));
        }
        return results;
    }

    public Set<PPI> fetch(int geneId) throws JAXBException, MalformedURLException {
        Logger.getLogger(MiMI.class.getName()).info("Fetching interactions for GeneID:" + geneId + "...");
        Unmarshaller um = JAXBContext.newInstance("org.mongkie.mimi.jaxb.official", getClass().getClassLoader()).createUnmarshaller();
        NCIBI ncibi = (NCIBI) um.unmarshal(new URL("http://mimi.ncibi.org/MimiWeb/fetch.jsp?geneid=" + geneId + "&type=interactions"));
        Set<PPI> interactions = new LinkedHashSet<PPI>();
        for (Result result : ncibi.getMiMI().getResponse().getResultSet().getResult()) {
            InteractingGene ig = result.getInteractingGene();
            if (ig.getTaxonomyID() == 9606) {
                interactions.add(new PPI(geneId, ig));
            }
        }
        return interactions;
    }

    @Override
    public Map<Integer, Attribute.Set> annotate(Integer... geneIds)
            throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        Map<Integer, Attribute.Set> attributes = new HashMap<Integer, Attribute.Set>();
        for (Integer geneId : geneIds) {
            attributes.put(geneId, search(geneId));
        }
        return attributes;
    }

    public Attribute.Set search(int geneId)
            throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        Logger.getLogger(MiMI.class.getName()).info("Searching annotations for GeneID:" + geneId + "...");
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse("http://mimi.ncibi.org/MimiWeb/fetch.jsp?search=" + geneId);
        xpath.reset();
        Node result = (Node) xpath.evaluate(
                "//Response/Result[GeneID=" + geneId + " and TaxonomyID=9606]", doc, XPathConstants.NODE);
        Attribute.Set attributes = new Attribute.Set();
        if (result != null) {
            NodeList elements = result.getChildNodes();
            for (int i = 0; i < elements.getLength(); i++) {
                Node e = elements.item(i);
                String name = e.getNodeName();
                try {
                    switch (AnnotationElement.valueOf(name)) {
                        case GeneID:
                        case TaxonomyID:
                            attributes.add(new Attribute(name, Integer.valueOf(e.getTextContent())));
                            break;
                        case GeneSymbol:
                        case GeneDescription:
                        case Organism:
                        case GeneType:
                            attributes.add(new Attribute(name, e.getTextContent()));
                            break;
                        default:
                            break;
                    }
                } catch (IllegalArgumentException ex) {
                }
            }
        }
        return attributes;
    }

    @Override
    public Class<Integer> getKeyType() {
        return Integer.class;
    }

    @Override
    public boolean isDirected() {
        return false;
    }

    public class PPI implements Interaction<Integer> {

        private final int sourceGeneId, targetGeneId;
        private final int interactionId;
        private final Interactor<Integer> interactor;
        private final Attribute.Set attributes;

        public PPI(Integer sourceGeneId, InteractingGene interactingGene) {
            this.sourceGeneId = sourceGeneId;
            interactionId = interactingGene.getInteractionID();
            targetGeneId = interactingGene.getGeneID().getValue();
            interactor = new Interactor<Integer>(targetGeneId, makeupTargetAttributes(interactingGene));
            attributes = makeupInteractionAttributes(interactingGene);
        }

        private Attribute.Set makeupTargetAttributes(InteractingGene interactingGene) {
            Attribute.Set attrs = new Attribute.Set();
            attrs.add(new Attribute(AnnotationElement.GeneID.name(), interactingGene.getGeneID().getValue()));
            attrs.add(new Attribute(AnnotationElement.GeneSymbol.name(), interactingGene.getGeneSymbol()));
            attrs.add(new Attribute(AnnotationElement.TaxonomyID.name(), interactingGene.getTaxonomyID()));
            return attrs;
        }

        private Attribute.Set makeupInteractionAttributes(InteractingGene interactingGene) {
            Attribute.Set attrs = new Attribute.Set();
            Map<String, List<String>> xmlAttrs = new LinkedHashMap<String, List<String>>();
            for (InteractionAttribute interactionAttr : interactingGene.getInteractionAttribute()) {
                String attr = interactionAttr.getType();
                String content = interactionAttr.getContent();
                if (attr.length() > 0 && content.length() > 0) {
                    List<String> values = xmlAttrs.get(attr);
                    if (values == null) {
                        values = new ArrayList<String>();
                        xmlAttrs.put(attr, values);
                    }
                    values.add(content);
                }
            }
            attrs.add(new Attribute(InteractionElement.InteractionID.name(), interactingGene.getInteractionID()));
            for (String name : xmlAttrs.keySet()) {
                attrs.add(new Attribute(name, xmlAttrs.get(name).toArray(new String[]{})));
            }
            return attrs;
        }

        public int getSourceGeneId() {
            return sourceGeneId;
        }

        public int getTargetGeneId() {
            return targetGeneId;
        }

        public int getInteractionId() {
            return interactionId;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final PPI other = (PPI) obj;
            return (this.sourceGeneId == other.sourceGeneId && this.targetGeneId == other.targetGeneId)
                    || (this.sourceGeneId == other.targetGeneId && this.targetGeneId == other.sourceGeneId);
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 59 * hash
                    + (this.sourceGeneId + this.targetGeneId) + Math.abs(this.sourceGeneId - this.targetGeneId);
            return hash;
        }

        @Override
        public Attribute.Set getAttributeSet() {
            return attributes;
        }

        @Override
        public Integer getSourceKey() {
            return sourceGeneId;
        }

        @Override
        public Interactor<Integer> getInteractor() {
            return interactor;
        }
    }

    public enum AnnotationElement {

        GeneID(int.class, -1),
        GeneSymbol(String.class, null),
        TaxonomyID(int.class, -1),
        Organism(String.class, null),
        GeneDescription(String.class, null),
        GeneType(String.class, null);
        private final Class type;
        private final Object defaultValue;

        private AnnotationElement(Class type, Object defaultValue) {
            this.type = type;
            this.defaultValue = defaultValue;
        }

        public Class getType() {
            return type;
        }

        public Object getDefaultValue() {
            return defaultValue;
        }

        @Override
        public String toString() {
            return name();
        }

        public static Schema createSchema() {
            int size = values().length;
            String[] names = new String[size];
            Class[] types = new Class[size];
            Object[] defaults = new Object[size];
            int i = 0;
            for (AnnotationElement e : values()) {
                names[i] = e.name();
                types[i] = e.getType();
                defaults[i] = e.getDefaultValue();
                i++;
            }
            Schema s = new Schema(names, types, defaults);
            s.setKeyField(GeneID.name());
            s.setLabelField(GeneSymbol.name());
            return s;
        }
    }

    public enum InteractionElement {

        InteractionID;

        @Override
        public String toString() {
            return name();
        }

        public static Schema createSchema() {
            int size = 1 + Attribute.values().length;
            String[] names = new String[size];
            Class[] types = new Class[size];
            Object[] defaults = new Object[size];
            names[0] = InteractionID.name();
            types[0] = int.class;
            defaults[0] = -1;
            int i = 1;
            for (Attribute e : Attribute.values()) {
                names[i] = e.name();
                types[i] = String[].class;
                defaults[i] = null;
                i++;
            }
            Schema s = new Schema(names, types, defaults);
            s.setKeyField(InteractionID.name());
            return s;
        }

        public enum Attribute {

            Component,
            Function,
            InteractionType,
            Process,
            Provenance,
            PubMed;

            @Override
            public String toString() {
                return name();
            }
        }
    }
}
