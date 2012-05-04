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
package org.mongkie.gobean.rest;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import gobean.calculation.EnrichmentMethod;
import gobean.statistics.MultipleTestCorrectionMethod;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.MediaType;
import org.mongkie.gobean.EnrichedResult;

/** Jersey REST client generated for REST resource:GoBeanResource [gobean]<br>
 *  USAGE:<pre>
 *        GoBeanService client = new GoBeanService(String baseUri, String path);
 *        Object response = client.XXX(...);
 *        // do whatever with response
 *        client.close();
 *  </pre>
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class GoBeanService {

    private final WebResource resource;
    private final Client client;

    public GoBeanService(String baseUri, String path) {
        client = Client.create(new DefaultClientConfig());
        resource = client.resource(baseUri).path(path);
    }

    public EnrichedResult getEnrichedResult(EnrichmentMethod strategy, MultipleTestCorrectionMethod correction, String... genes) {
        return getEnrichedResult(strategy, correction, 1.0D, genes);
    }

    public EnrichedResult getEnrichedResult(EnrichmentMethod strategy, MultipleTestCorrectionMethod correction, double pCutoff, String... genes) {
        Logger.getLogger(GoBeanService.class.getName()).log(Level.INFO,
                "GO enrichment testing... [{0}, {1}, Max-p:{2}]", new Object[]{strategy, correction, pCutoff});
        long start = System.currentTimeMillis();

        String geness = Arrays.toString(genes);
        EnrichedResult result = getXml(EnrichedResult.class, strategy.toString(), correction.getName(), pCutoff, geness.substring(1, geness.length() - 1).replaceAll(", ", ","));

        long elapsed = System.currentTimeMillis() - start;
        Logger.getLogger(GoBeanService.class.getName()).log(Level.INFO,
                "GO enrichment completed: {0}.{1} seconds. Number of selected GO terms: {2}", new Object[]{elapsed / 1000, elapsed % 1000, result.getSelectedGoIds().size()});

        return result;
    }

    private <T> T getXml(Class<T> responseType,
            String strategy, String correction, double pCutoff, String genes) throws UniformInterfaceException {
        return resource.queryParam(QueryParam.ENRICHMENT_STRATEGY, strategy).
                queryParam(QueryParam.MULTIPLE_TESTING_CORRECTION, correction).
                queryParam(QueryParam.PVALUE_MAX, String.valueOf(pCutoff)).
                queryParam(QueryParam.GENES, genes).
                accept(MediaType.APPLICATION_XML).get(responseType);
    }

    public void close() {
        client.destroy();
    }

    public static GoBeanService getDefault() {
        return DefaultHolder.DEFAULT;
    }

    private static class DefaultHolder {

        private static final String PROP_FILE = GoBeanService.class.getSimpleName() + ".properties";
        private static String baseUri, path;

        static {
            try {
                Properties props = new Properties();
                props.load(GoBeanService.class.getResourceAsStream(PROP_FILE));
                baseUri = props.getProperty("baseUri");
                path = props.getProperty("path");
            } catch (IOException ex) {
                Logger.getLogger(GoBeanService.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        private static String getBaseUri() {
            if (baseUri == null || baseUri.length() == 0) {
                throw new RuntimeException("Please specify the base URI in the " + PROP_FILE + " file.");
            }

            return baseUri;
        }

        private static String getPath() {
            return (path == null) ? "" : path;
        }
        private static final GoBeanService DEFAULT = new GoBeanService(getBaseUri(), getPath());
    }
}
