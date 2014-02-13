package org.triple_brain.module.model.graph.neo4j;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import org.neo4j.cypher.ExecutionEngine;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.factory.GraphDatabaseSetting;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.graphdb.index.ReadableIndex;
import org.neo4j.kernel.logging.BufferingLogger;
import org.neo4j.test.TestGraphDatabaseFactory;
import org.triple_brain.module.model.FriendlyResourceFactory;
import org.triple_brain.module.model.WholeGraph;
import org.triple_brain.module.model.graph.FriendlyResourceOperator;
import org.triple_brain.module.model.graph.GraphComponentTest;
import org.triple_brain.module.model.graph.GraphFactory;
import org.triple_brain.module.model.graph.edge.EdgeFactory;
import org.triple_brain.module.model.graph.edge.EdgeOperator;
import org.triple_brain.module.model.graph.vertex.VertexFactory;
import org.triple_brain.module.model.graph.vertex.VertexInSubGraphOperator;
import org.triple_brain.module.model.suggestion.SuggestionFactory;
import org.triple_brain.module.model.suggestion.SuggestionOperator;
import org.triple_brain.module.neo4j_graph_manipulator.graph.*;

/*
* Copyright Mozilla Public License 1.1
*/
public class Neo4JTestModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(WholeGraph.class).to(Neo4jWholeGraph.class);
        GraphDatabaseService graphDb = new TestGraphDatabaseFactory()
                .newImpermanentDatabaseBuilder()
                .setConfig(
                        GraphDatabaseSettings.node_keys_indexable,
                        Neo4jUserGraph.URI_PROPERTY_NAME
                )
                .setConfig(
                        GraphDatabaseSettings.node_auto_indexing,
                        GraphDatabaseSetting.TRUE
                )
                .setConfig(
                        GraphDatabaseSettings.relationship_keys_indexable,
                        Neo4jUserGraph.URI_PROPERTY_NAME
                )
                .setConfig(
                        GraphDatabaseSettings.relationship_auto_indexing,
                        GraphDatabaseSetting.TRUE
                ).newGraphDatabase();

        bind(GraphDatabaseService.class).toInstance(
                graphDb
        );
        bind(ExecutionEngine.class).toInstance(
                new ExecutionEngine(graphDb, new BufferingLogger())
        );

        FactoryModuleBuilder factoryModuleBuilder = new FactoryModuleBuilder();

        install(factoryModuleBuilder
                .build(Neo4jEdgeFactory.class));

        install(factoryModuleBuilder
                .build(Neo4jUserGraphFactory.class));

        install(factoryModuleBuilder
                .implement(VertexInSubGraphOperator.class, Neo4jVertexInSubGraphOperator.class)
                .build(VertexFactory.class));

        install(factoryModuleBuilder
                .implement(EdgeOperator.class, Neo4jEdgeOperator.class)
                .build(EdgeFactory.class));

        install(factoryModuleBuilder
                .build(Neo4jVertexFactory.class));

        install(factoryModuleBuilder
                .build(Neo4jSubGraphExtractorFactory.class));

        install(factoryModuleBuilder
                .build(Neo4jGraphElementFactory.class));

        install(factoryModuleBuilder
                .implement(FriendlyResourceOperator.class, Neo4jFriendlyResource.class)
                .build(FriendlyResourceFactory.class)
        );
        install(factoryModuleBuilder
                .build(Neo4jFriendlyResourceFactory.class)
        );
        install(factoryModuleBuilder
                .implement(SuggestionOperator.class, Neo4jSuggestion.class)
                .build(SuggestionFactory.class)
        );
        install(factoryModuleBuilder
                .build(Neo4jSuggestionFactory.class)
        );
        install(factoryModuleBuilder
                .build(Neo4jSuggestionOriginFactory.class)
        );
        bind(GraphComponentTest.class).toInstance(
                new Neo4JGraphComponentTest()
        );
        bind(new TypeLiteral<ReadableIndex<Node>>() {
        }).toInstance(
                graphDb.index()
                        .getNodeAutoIndexer()
                        .getAutoIndex()
        );

        bind(new TypeLiteral<ReadableIndex<Relationship>>() {
        }).toInstance(
                graphDb.index()
                        .getRelationshipAutoIndexer()
                        .getAutoIndex()
        );

        bind(GraphFactory.class).to(Neo4jGraphFactory.class);
        requireBinding(Neo4jUtils.class);
    }
}
