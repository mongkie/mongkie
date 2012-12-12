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
package org.mongkie.enrichment.go;

import gobean.calculation.EnrichmentMethod;
import gobean.statistics.MultipleTestCorrectionMethod;
import org.mongkie.enrichment.spi.EnrichedResultUI;
import org.mongkie.enrichment.spi.Enrichment;
import org.mongkie.enrichment.spi.EnrichmentBuilder;
import org.mongkie.gobean.EnrichedResult;
import org.mongkie.gobean.rest.GoBeanService;
import org.mongkie.ui.enrichment.go.EnrichedResultView;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class GoEnrichment implements Enrichment {

    private final GoEnrichmentBuilder builder;
    private volatile boolean canceled = false;
    private EnrichmentMethod strategy = EnrichmentMethod.Classic;
    private MultipleTestCorrectionMethod correction = MultipleTestCorrectionMethod.None;
    private double pmax = 0.01D;

    public GoEnrichment(GoEnrichmentBuilder builder) {
        this.builder = builder;
    }

    @Override
    public EnrichedResultUI execute(String... genes) {
        canceled = false;

        EnrichedResult result = GoBeanService.getDefault().getEnrichedResult(strategy, correction, pmax, genes);

        if (result == null || canceled) {
            return null;
        }
        return new EnrichedResultView(result);
    }

    @Override
    public boolean cancel() {
        canceled = true;
        return false;
    }

    @Override
    public EnrichmentBuilder getBuilder() {
        return builder;
    }

    public MultipleTestCorrectionMethod getCorrection() {
        return correction;
    }

    public void setCorrection(MultipleTestCorrectionMethod correction) {
        this.correction = correction;
    }

    public EnrichmentMethod getStrategy() {
        return strategy;
    }

    public void setStrategy(EnrichmentMethod strategy) {
        this.strategy = strategy;
    }

    public double getMaxP() {
        return pmax;
    }

    public void setMaxP(double pmax) {
        this.pmax = pmax;
    }
}
