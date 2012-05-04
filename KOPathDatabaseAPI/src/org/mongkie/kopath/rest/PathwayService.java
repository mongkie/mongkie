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
package org.mongkie.kopath.rest;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import java.util.Arrays;
import java.util.List;
import org.mongkie.kopath.Pathway;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.MediaType;
import kobic.prefuse.data.io.ReaderFactory;
import org.mongkie.kopath.rest.io.RestConnection;
import org.mongkie.kopath.rest.io.RestResponse;
import org.mongkie.kopath.util.Utilities;
import prefuse.data.Graph;
import prefuse.data.io.DataIOException;
import static org.mongkie.kopath.Config.*;

/**
 * 
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class PathwayService {

    private static String serviceUrl;
    private static final String PROP_FILE = PathwayService.class.getSimpleName() + ".properties";

    static {
        try {
            Properties props = new Properties();
            props.load(PathwayService.class.getResourceAsStream(PROP_FILE));
            serviceUrl = props.getProperty("serviceUrl");
        } catch (IOException ex) {
            Logger.getLogger(PathwayService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static String getServiceUrl() throws IOException {
        if (serviceUrl == null || serviceUrl.length() == 0) {
            throw new IOException("Please specify the service url in the " + PROP_FILE + " file.");
        }

        return serviceUrl;
    }

    public static void setServiceUrl(String serviceUrl) {
        PathwayService.serviceUrl = serviceUrl;
    }

    public static Graph getGraphFromSuperPathway(String xTableName, String[] geneInput, String[] pathwayInput) throws IOException, DataIOException {
        String[][] pathParams = new String[][]{{"{serviceUrl}", getServiceUrl()},
            {"{xTableName}", xTableName},
            {"{geneInput}", Utilities.getCsvStringFrom(geneInput)},
            {"{pathwayInput}", Utilities.getCsvStringFrom(pathwayInput)}};
        RestConnection conn = new RestConnection("{serviceUrl}/superpathway?xTableName={xTableName}&geneInput={geneInput}&pathwayInput={pathwayInput}", pathParams, null);
        RestResponse response = conn.get();
        if (response.getResponseCode() == 200) {
            Graph g = ReaderFactory.getDefaultGraphMLReader().readGraph(new ByteArrayInputStream(response.getDataAsString().getBytes()));
            g.getNodeTable().index(FIELD_UID);
            return g;
        }
        return null;
    }

    public static String getGraphML(int dbId, String... pathwayIds) throws IOException {
        String[][] pathParams = new String[][]{{"{serviceUrl}", getServiceUrl()}, {"{dbId}", String.valueOf(dbId)}, {"{pathwayIds}", Utilities.getCsvStringFrom(pathwayIds)}};
        RestConnection conn = new RestConnection("{serviceUrl}/pathway/{dbId}/{pathwayIds}", pathParams, null);
        RestResponse response = conn.get();
        return (response.getResponseCode() == 200) ? response.getDataAsString() : null;
    }

    public static Graph getGraph(int dbId, String... pathwayIds) throws IOException, DataIOException {
        Graph g = ReaderFactory.getDefaultGraphMLReader().readGraph(new ByteArrayInputStream(getGraphML(dbId, pathwayIds).getBytes()));
        g.getNodeTable().index(FIELD_UID);
        return g;
    }

    public static int countPathway(int dbId, String... genes) throws IOException {
        String[][] pathParams = new String[][]{{"{serviceUrl}", getServiceUrl()}, {"{dbId}", String.valueOf(dbId)}, {"{genes}", Utilities.getCsvStringFrom(genes)}};
        RestConnection conn = new RestConnection("{serviceUrl}/count?db={dbId}&genes={genes}", pathParams, null);
        RestResponse response = conn.get();
        return (response.getResponseCode() == 200) ? Integer.parseInt(response.getDataAsString()) : 0;
    }

    public static List<Pathway> searchPathway(int dbId, String... genes) throws IOException {
        return Arrays.asList(getJerseyClient().resource(getServiceUrl()).path("search").
                queryParam("db", String.valueOf(dbId)).
                queryParam("genes", Utilities.getCsvStringFrom(genes)).
                accept(MediaType.APPLICATION_XML).get(Pathway[].class));
    }

    public static int countPathway(int dbId) throws IOException {
        return Integer.parseInt(getJerseyClient().resource(getServiceUrl()).path("count").
                queryParam("db", String.valueOf(dbId)).
                accept(MediaType.TEXT_PLAIN).get(String.class));
    }

    public static List<Pathway> searchPathway(int dbId) throws IOException {
        return Arrays.asList(getJerseyClient().resource(getServiceUrl()).path("search").
                queryParam("db", String.valueOf(dbId)).
                accept(MediaType.APPLICATION_XML).get(Pathway[].class));
    }

    public static int countPathway(int dbId, String pathway) throws IOException {
        return countPathway(dbId, pathway, false);
    }

    public static int countPathway(int dbId, String pathway, boolean like) throws IOException {
        return Integer.parseInt(getJerseyClient().resource(getServiceUrl()).path("count").
                queryParam("db", String.valueOf(dbId)).
                queryParam("pathway", pathway).
                queryParam("like", String.valueOf(like)).
                accept(MediaType.TEXT_PLAIN).get(String.class));
    }

    public static List<Pathway> searchPathway(int dbId, String pathway) throws IOException {
        return searchPathway(dbId, pathway, false);
    }

    public static List<Pathway> searchPathway(int dbId, String pathway, boolean like) throws IOException {
        return Arrays.asList(getJerseyClient().resource(getServiceUrl()).path("search").
                queryParam("db", String.valueOf(dbId)).
                queryParam("pathway", pathway).
                queryParam("like", String.valueOf(like)).
                accept(MediaType.APPLICATION_XML).get(Pathway[].class));
    }

    public static void close() {
        getJerseyClient().destroy();
    }

    public static Client getJerseyClient() {
        return ClientHolder.client;
    }

    private static class ClientHolder {

        private static final Client client = Client.create(new DefaultClientConfig());
    }
}
