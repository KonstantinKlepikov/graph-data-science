/*
 * Copyright (c) 2017-2020 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.graphalgo.betweenness;

import org.neo4j.graphalgo.AlgoBaseProc;
import org.neo4j.graphalgo.AlgorithmFactory;
import org.neo4j.graphalgo.MutateProc;
import org.neo4j.graphalgo.config.GraphCreateConfig;
import org.neo4j.graphalgo.core.CypherMapWrapper;
import org.neo4j.graphalgo.core.utils.paged.HugeAtomicDoubleArray;
import org.neo4j.graphalgo.core.write.PropertyTranslator;
import org.neo4j.graphalgo.result.AbstractResultBuilder;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.neo4j.graphalgo.betweenness.BetweennessCentralityProc.BETWEENNESS_DESCRIPTION;
import static org.neo4j.procedure.Mode.READ;

public class BetweennessCentralityMutateProc extends MutateProc<BetweennessCentrality, HugeAtomicDoubleArray, BetweennessCentralityMutateProc.MutateResult, BetweennessCentralityMutateConfig> {

    @Procedure(value = "gds.betweenness.mutate", mode = READ)
    @Description(BETWEENNESS_DESCRIPTION)
    public Stream<MutateResult> mutate(
        @Name(value = "graphName") Object graphNameOrConfig,
        @Name(value = "configuration", defaultValue = "{}") Map<String, Object> configuration
    ) {
        return mutate(compute(graphNameOrConfig, configuration));
    }

    @Override
    protected BetweennessCentralityMutateConfig newConfig(
        String username,
        Optional<String> graphName,
        Optional<GraphCreateConfig> maybeImplicitCreate,
        CypherMapWrapper config
    ) {
        return BetweennessCentralityMutateConfig.of(username, graphName, maybeImplicitCreate, config);
    }

    @Override
    protected AlgorithmFactory<BetweennessCentrality, BetweennessCentralityMutateConfig> algorithmFactory(
        BetweennessCentralityMutateConfig config
    ) {
        return BetweennessCentralityProc.algorithmFactory(config);
    }

    @Override
    protected PropertyTranslator<HugeAtomicDoubleArray> nodePropertyTranslator(AlgoBaseProc.ComputationResult<BetweennessCentrality, HugeAtomicDoubleArray, BetweennessCentralityMutateConfig> computationResult) {
        return BetweennessCentralityProc.propertyTranslator();
    }

    @Override
    protected AbstractResultBuilder<MutateResult> resultBuilder(ComputationResult<BetweennessCentrality, HugeAtomicDoubleArray, BetweennessCentralityMutateConfig> computeResult) {
        return BetweennessCentralityProc.resultBuilder(new MutateResult.Builder(), computeResult, callContext);
    }

    public static final class MutateResult {

        public final long nodePropertiesWritten;
        public final long createMillis;
        public final long computeMillis;
        public final long mutateMillis;

        public final double minCentrality;
        public final double maxCentrality;
        public final double sumCentrality;

        MutateResult(
            long nodePropertiesWritten,
            long createMillis,
            long computeMillis,
            long mutateMillis,
            double minCentrality,
            double maxCentrality,
            double sumCentrality
        ) {
            this.nodePropertiesWritten = nodePropertiesWritten;
            this.createMillis = createMillis;
            this.computeMillis = computeMillis;
            this.mutateMillis = mutateMillis;

            this.minCentrality = minCentrality;
            this.maxCentrality = maxCentrality;
            this.sumCentrality = sumCentrality;
        }

        static final class Builder extends BetweennessCentralityProc.BetweennessCentralityResultBuilder<MutateResult> {

            @Override
            public MutateResult build() {
                return new MutateResult(
                    nodePropertiesWritten,
                    createMillis,
                    computeMillis,
                    mutateMillis,
                    minCentrality,
                    maxCentrality,
                    sumCentrality
                );
            }
        }
    }
}