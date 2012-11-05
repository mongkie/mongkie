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
package org.mongkie.mimi;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import kobic.prefuse.data.Attribute;
import kobic.prefuse.data.Schema;
import org.mongkie.im.spi.Interaction.Interactor;
import org.mongkie.im.spi.InteractionSource;
import org.mongkie.mimi.jaxb.annotation.Annotation;
import org.mongkie.mimi.jaxb.interaction.Interaction;
import org.mongkie.mimi.jaxb.interaction.Result;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
@ServiceProvider(service = InteractionSource.class, position = 10)
public class MiMI implements InteractionSource<Integer> {

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
        if (geneIds.length > 0) {
            Logger.getLogger(MiMI.class.getName()).log(Level.INFO, "Fetching interactions for the selected genes...");
            org.mongkie.mimi.jaxb.interaction.ResultSet rs = (org.mongkie.mimi.jaxb.interaction.ResultSet) JAXBContext.newInstance("org.mongkie.mimi.jaxb.interaction", getClass().getClassLoader()).createUnmarshaller()
                    .unmarshal(new URL("http://api.mongkie.org/mimi/q?genes=" + Arrays.toString(geneIds).replaceAll("^\\[|\\]$", "").replaceAll(", ", ",")));
            for (Result r : rs.getResult()) {
                Set<PPI> interactions = new LinkedHashSet<PPI>();
                for (Interaction i : r.getInteraction()) {
                    interactions.add(new PPI(i));
                }
                results.put(r.getSourceKey(), interactions);
            }
        }
        return results;
    }

    @Override
    public Map<Integer, Attribute.Set> annotate(Integer... geneIds) throws JAXBException, MalformedURLException {
        Map<Integer, Attribute.Set> results = new HashMap<Integer, Attribute.Set>();
        if (geneIds.length > 0) {
            Logger.getLogger(MiMI.class.getName()).log(Level.INFO, "Fetching annotations for the selected genes...");
            org.mongkie.mimi.jaxb.annotation.ResultSet rs = (org.mongkie.mimi.jaxb.annotation.ResultSet) JAXBContext.newInstance("org.mongkie.mimi.jaxb.annotation", getClass().getClassLoader()).createUnmarshaller()
                    .unmarshal(new URL("http://api.mongkie.org/mimi/a?genes=" + Arrays.toString(geneIds).replaceAll("^\\[|\\]$", "").replaceAll(", ", ",")));
            for (Annotation a : rs.getAnnotation()) {
                Attribute.Set attributes = new Attribute.Set();
                attributes.add(new Attribute(AnnotationElement.GeneID.name(), Integer.valueOf(a.getGeneID())));
                attributes.add(new Attribute(AnnotationElement.TaxonomyID.name(), Integer.valueOf(a.getTaxonomyID())));
                attributes.add(new Attribute(AnnotationElement.GeneSymbol.name(), a.getGeneSymbol()));
                attributes.add(new Attribute(AnnotationElement.GeneDescription.name(), a.getGeneDescription()));
                attributes.add(new Attribute(AnnotationElement.GeneType.name(), a.getGeneType()));
                attributes.add(new Attribute(AnnotationElement.Organism.name(), a.getOrganism()));
                results.put(a.getSourceKey(), attributes);
            }
        }
        return results;
    }

    @Override
    public Class<Integer> getKeyType() {
        return Integer.class;
    }

    @Override
    public boolean isDirected() {
        return false;
    }

    public class PPI implements org.mongkie.im.spi.Interaction<Integer> {

        private final int sourceGeneId, targetGeneId;
        private final int interactionId;
        private final Interactor<Integer> interactor;
        private final Attribute.Set attributes;

        public PPI(Interaction interaction) {
            interactionId = interaction.getID();
            sourceGeneId = interaction.getSource();
            targetGeneId = interaction.getTarget();
            interactor = new Interactor<Integer>(targetGeneId, makeupTargetAttributes(interaction));
            attributes = makeupInteractionAttributes(interaction);
        }

        private Attribute.Set makeupTargetAttributes(Interaction interaction) {
            Attribute.Set attrs = new Attribute.Set();
            attrs.add(new Attribute(AnnotationElement.GeneID.name(), Integer.valueOf(interaction.getTarget())));
            return attrs;
        }

        private Attribute.Set makeupInteractionAttributes(Interaction interaction) {
            Attribute.Set attrs = new Attribute.Set();
            attrs.add(new Attribute(InteractionElement.InteractionID.name(), Integer.valueOf(interaction.getID())));
            for (InteractionElement.Attribute a : InteractionElement.Attribute.values()) {
                attrs.add(new Attribute(a.name(), InteractionElement.Attribute.valueOf(interaction, a).toArray(new String[]{})));
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

            public static List<String> valueOf(Interaction i, Attribute a) {
                switch (a) {
                    case Component:
                        return i.getComponent();
                    case Function:
                        return i.getFunction();
                    case InteractionType:
                        return i.getInteractionType();
                    case Process:
                        return i.getProcess();
                    case Provenance:
                        return i.getProvenance();
                    case PubMed:
                        return i.getPubMed();
                    default:
                        throw new IllegalArgumentException();
                }
            }
        }
    }
}
