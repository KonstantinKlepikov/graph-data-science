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
package org.neo4j.gds.embeddings.graphsage.proc;

import org.neo4j.gds.embeddings.graphsage.algo.GraphSageTrain;
import org.neo4j.gds.embeddings.graphsage.algo.GraphSageTrainConfig;
import org.neo4j.graphalgo.AlgorithmFactory;
import org.neo4j.graphalgo.AlphaAlgorithmFactory;
import org.neo4j.graphalgo.TrainProc;
import org.neo4j.graphalgo.config.GraphCreateConfig;
import org.neo4j.graphalgo.core.CypherMapWrapper;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Mode;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.neo4j.gds.embeddings.graphsage.proc.GraphSageStreamProc.GRAPHSAGE_DESCRIPTION;
import static org.neo4j.graphalgo.config.TrainConfig.ALGO_TYPE_KEY;
import static org.neo4j.graphalgo.config.TrainConfig.MODEL_NAME_KEY;

public class GraphSageTrainProc extends TrainProc<GraphSageTrain, GraphSageTrain.TrainedModel, GraphSageTrainProc.TrainResult, GraphSageTrainConfig> {

    @Description(GRAPHSAGE_DESCRIPTION)
    @Procedure(name = "gds.alpha.graphSage.train", mode = Mode.READ)
    public Stream<TrainResult> train(
        @Name(value = "graphName") Object graphNameOrConfig,
        @Name(value = "configuration", defaultValue = "{}") Map<String, Object> configuration
    ) {
        return Stream.of(trainResult(compute(graphNameOrConfig, configuration)));
    }

    @Override
    protected TrainResult trainResult(ComputationResult<GraphSageTrain, GraphSageTrain.TrainedModel, GraphSageTrainConfig> computationResult) {
        return new TrainResult(computationResult.result(), computationResult.config(), computationResult.computeMillis());
    }

    @Override
    protected GraphSageTrainConfig newConfig(
        String username,
        Optional<String> graphName,
        Optional<GraphCreateConfig> maybeImplicitCreate,
        CypherMapWrapper config
    ) {
        return GraphSageTrainConfig.of(
            username, graphName, maybeImplicitCreate, config);
    }

    @Override
    protected AlgorithmFactory<GraphSageTrain, GraphSageTrainConfig> algorithmFactory() {
        return (AlphaAlgorithmFactory<GraphSageTrain, GraphSageTrainConfig>) (graph, configuration, tracker, log) -> new GraphSageTrain(
            graph, configuration, log);
    }

    public static class TrainResult {

        public final String graphName;
        public final String graphCreateConfig;
        public final Map<String, Object> modelInfo;
        public final Map<String, Object> configuration;
        public final long trainMillis;

        TrainResult(
            GraphSageTrain.TrainedModel trainedModel,
            GraphSageTrainConfig graphSageTrainConfig,
            long trainMillis
        ) {
            this.graphName = graphSageTrainConfig.graphName().orElse("");
            this.graphCreateConfig = "";//graphSageTrainConfig.implicitCreateConfig().orElse(null);
            this.modelInfo = new HashMap<>();
            modelInfo.put(MODEL_NAME_KEY, trainedModel.modelName());
            modelInfo.put(ALGO_TYPE_KEY, trainedModel.algoType());
            this.configuration = graphSageTrainConfig.toMap();
            this.trainMillis = trainMillis;
        }
    }
}
