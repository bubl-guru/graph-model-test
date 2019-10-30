/*
 * Copyright Vincent Blouin under the GPL License version 3
 */

package guru.bubl.test.module.model.graph.search;

import com.google.inject.Inject;
import guru.bubl.module.model.graph.GraphElementOperator;
import guru.bubl.module.model.graph.GraphElementPojo;
import guru.bubl.module.model.graph.edge.EdgeOperator;
import guru.bubl.module.model.graph.identification.IdentifierPojo;
import guru.bubl.module.model.graph.schema.SchemaOperator;
import guru.bubl.module.model.graph.schema.SchemaPojo;
import guru.bubl.module.model.graph.vertex.VertexFactory;
import guru.bubl.module.model.graph.vertex.VertexOperator;
import guru.bubl.module.model.search.GraphElementSearchResult;
import guru.bubl.module.model.search.GraphIndexer;
import guru.bubl.test.module.utils.search.Neo4jSearchRelatedTest;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class GraphIndexerTest extends Neo4jSearchRelatedTest {

    @Inject
    GraphIndexer graphIndexer;

    @Inject
    VertexFactory vertexFactory;

    @Test
    public void index_vertex_sets_its_private_surround_graph() {
        GraphElementSearchResult vertexSearchResult = graphSearchFactory.usingSearchTerm(
                vertexB.label()
        ).searchForAnyResourceThatCanBeUsedAsAnIdentifier(
                user
        ).iterator().next();
        assertTrue(
                vertexSearchResult.getContext().isEmpty()
        );
        graphIndexer.indexVertex(vertexB);
        vertexSearchResult = graphSearchFactory.usingSearchTerm(
                vertexB.label()
        ).searchForAnyResourceThatCanBeUsedAsAnIdentifier(
                user
        ).iterator().next();
        assertFalse(
                vertexSearchResult.getContext().isEmpty()
        );
    }

    @Test
    public void limits_the_context_size_of_vertices() {
        for (int i = 0; i < 5; i++) {
            vertexFactory.withUri(
                    vertexB.addVertexAndRelation().destinationVertex().uri()
            ).label("vertex " + i);
        }
        graphIndexer.indexVertex(vertexB);
        GraphElementSearchResult vertexSearchResult = graphSearchFactory.usingSearchTerm(
                vertexB.label()
        ).searchForAnyResourceThatCanBeUsedAsAnIdentifier(
                user
        ).iterator().next();
        assertThat(
                vertexSearchResult.getContext().size(),
                is(5)
        );
    }

    @Test
    public void filters_empty_label_from_context() {
        for (int i = 0; i < 5; i++) {
            vertexB.addVertexAndRelation();
        }
        graphIndexer.indexVertex(vertexB);
        GraphElementSearchResult vertexSearchResult = graphSearchFactory.usingSearchTerm(
                vertexB.label()
        ).searchForAnyResourceThatCanBeUsedAsAnIdentifier(
                user
        ).iterator().next();
        assertThat(
                vertexSearchResult.getContext().size(),
                is(2)
        );
    }

    @Test
    public void context_can_have_quotes() {
        vertexA.label("\"some\" label");
        graphIndexer.indexVertex(vertexB);
        GraphElementSearchResult vertexSearchResult = graphSearchFactory.usingSearchTerm(
                vertexB.label()
        ).searchForAnyResourceThatCanBeUsedAsAnIdentifier(
                user
        ).iterator().next();
        assertThat(
                vertexSearchResult.getContext().size(),
                is(2)
        );
    }

    @Test
    public void context_prioritize_vertices_with_most_child() {
        for (int i = 4; i <= 10; i++) {
            VertexOperator destinationVertex = vertexFactory.withUri(
                    vertexB.addVertexAndRelation().destinationVertex().uri()
            );
            vertexFactory.withUri(
                    destinationVertex.uri()
            ).label("vertex " + i);
            for (int j = 0; j <= i; j++) {
                destinationVertex.addVertexAndRelation();
            }
        }
        graphIndexer.indexVertex(vertexB);
        GraphElementSearchResult vertexSearchResult = graphSearchFactory.usingSearchTerm(
                vertexB.label()
        ).searchForAnyResourceThatCanBeUsedAsAnIdentifier(
                user
        ).iterator().next();
        String[] context = vertexSearchResult.getContext().values().toArray(
                new String[
                        vertexSearchResult.getContext().values().size()
                        ]
        );
        assertThat(
                context[0],
                is("vertex 10")
        );
        assertThat(
                context[1],
                is("vertex 9")
        );
        assertThat(
                context[4],
                is("vertex 6")
        );
    }

    @Test
    public void surround_graph_does_not_include_all_vertices() {
        graphIndexer.indexVertex(vertexA);
        GraphElementSearchResult vertexSearchResult = graphSearchFactory.usingSearchTerm(
                vertexA.label()
        ).searchForAnyResourceThatCanBeUsedAsAnIdentifier(
                user
        ).iterator().next();
        assertThat(
                vertexSearchResult.getContext().size(),
                is(1)
        );
    }

    @Test
    public void index_vertex_sets_its_public_surround_graph() {
        vertexB.makePublic();
        vertexA.makePublic();
        graphIndexer.indexVertex(vertexB);
        GraphElementSearchResult vertexSearchResult = graphSearchFactory.usingSearchTerm(
                vertexB.label()
        ).searchForAnyResourceThatCanBeUsedAsAnIdentifier(
                anotherUser
        ).iterator().next();
        assertThat(
                vertexSearchResult.getContext().size(),
                is(1)
        );
        vertexC.makePublic();
        graphIndexer.indexVertex(vertexB);
        vertexSearchResult = graphSearchFactory.usingSearchTerm(
                vertexB.label()
        ).searchForAnyResourceThatCanBeUsedAsAnIdentifier(
                anotherUser
        ).iterator().next();
        assertFalse(
                vertexSearchResult.getContext().isEmpty()
        );
        assertThat(
                vertexSearchResult.getContext().size(),
                is(2)
        );
    }

    @Test
    public void context_does_not_include_self_vertex() {
        graphIndexer.indexVertex(vertexB);
        GraphElementSearchResult vertexSearchResult = graphSearchFactory.usingSearchTerm(
                vertexB.label()
        ).searchForAnyResourceThatCanBeUsedAsAnIdentifier(
                user
        ).iterator().next();
        assertFalse(
                vertexSearchResult.getContext().containsKey(
                        vertexB.uri()
                )
        );
    }

    @Test
    @Ignore("schema feature is suspended")
    public void index_schema_sets_the_properties_as_context() {
        SchemaOperator schema = createSchema(userGraph.user());
        schema.label("schema1");
        graphIndexer.indexSchema(userGraph.schemaPojoWithUri(
                schema.uri()
        ));
        GraphElementSearchResult searchResult = graphSearchFactory.usingSearchTerm(
                "schema"
        ).searchForAnyResourceThatCanBeUsedAsAnIdentifier(
                user
        ).iterator().next();
        assertTrue(
                searchResult.getContext().isEmpty()
        );
        schema.addProperty().label("property 1");
        schema.addProperty().label("property 2");
        graphIndexer.indexSchema(userGraph.schemaPojoWithUri(
                schema.uri()
        ));
        searchResult = graphSearchFactory.usingSearchTerm(
                "schema"
        ).searchForAnyResourceThatCanBeUsedAsAnIdentifier(
                user
        ).iterator().next();
        assertFalse(
                searchResult.getContext().isEmpty()
        );
    }

    @Test
    public void index_relation_sets_source_and_destination_vertex_as_context() {
        EdgeOperator edgeAAndB = vertexA.getEdgeThatLinksToDestinationVertex(vertexB);
        GraphElementSearchResult searchResult = graphSearchFactory.usingSearchTerm(
                edgeAAndB.label()
        ).searchRelationsForAutoCompletionByLabel(
                user
        ).iterator().next();
        assertTrue(
                searchResult.getContext().isEmpty()
        );
        graphIndexer.indexRelation(edgeAAndB);
        searchResult = graphSearchFactory.usingSearchTerm(
                edgeAAndB.label()
        ).searchRelationsForAutoCompletionByLabel(
                user
        ).iterator().next();
        assertTrue(
                searchResult.getContext().containsValue(
                        vertexA.label()
                )
        );
        assertTrue(
                searchResult.getContext().containsValue(
                        vertexB.label()
                )
        );
    }

    @Test
    public void public_context_of_relation_is_empty_if_relation_is_private() {
        vertexA.makePublic();
        vertexB.makePublic();
        EdgeOperator edgeAAndB = vertexA.getEdgeThatLinksToDestinationVertex(vertexB);
        edgeAAndB.label("AAndB");
        graphIndexer.indexRelation(edgeAAndB);
        GraphElementSearchResult searchResult = graphSearchFactory.usingSearchTerm(
                edgeAAndB.label()
        ).searchRelationsForAutoCompletionByLabel(
                anotherUser
        ).iterator().next();
        assertFalse(
                searchResult.getContext().isEmpty()
        );
        vertexA.makePrivate();
        graphIndexer.indexRelation(edgeAAndB);

        List<GraphElementSearchResult> graphElementSearchResults = graphSearchFactory.usingSearchTerm(
                edgeAAndB.label()
        ).searchRelationsForAutoCompletionByLabel(
                anotherUser
        );
        assertTrue(
                graphElementSearchResults.isEmpty()
        );
    }

    @Test
    @Ignore("schema feature is suspended")
    public void index_property_sets_schema_as_context() {
        SchemaOperator schema = createSchema(userGraph.user());
        schema.label("schema1");
        GraphElementOperator property = schema.addProperty();
        property.label("a property");
        SchemaPojo schemaPojo = userGraph.schemaPojoWithUri(
                schema.uri()
        );
        GraphElementPojo propertyPojo = schemaPojo.getProperties().values().iterator().next();
        graphIndexer.indexProperty(propertyPojo, schemaPojo);
        GraphElementSearchResult graphElementSearchResult = graphSearchFactory.usingSearchTerm(
                "a property"
        ).searchRelationsForAutoCompletionByLabel(
                user
        ).iterator().next();
        assertTrue(
                graphElementSearchResult.getContext().containsValue(
                        "schema1"
                )
        );
    }

    @Test
    public void meta_context_includes_label_of_surround_vertices() {
        IdentifierPojo meta = vertexA.addMeta(
                modelTestScenarios.person()
        ).values().iterator().next();
        graphIndexer.indexMeta(meta);
        GraphElementSearchResult graphElementSearchResult = graphSearchFactory.usingSearchTerm(
                "Person"
        ).searchRelationsForAutoCompletionByLabel(
                user
        ).iterator().next();
        assertTrue(
                graphElementSearchResult.getContext().values().iterator().next().equals(
                        "vertex Azure"
                )
        );
    }

    @Test
    public void meta_related_to_relation_context_includes_label_of_surround_vertices() {
        EdgeOperator edge = vertexB.getEdgeThatLinksToDestinationVertex(vertexC);
        IdentifierPojo meta = edge.addMeta(
                modelTestScenarios.toDo()
        ).values().iterator().next();
        graphIndexer.indexMeta(meta);
        GraphElementSearchResult graphElementSearchResult = graphSearchFactory.usingSearchTerm(
                "To do"
        ).searchRelationsForAutoCompletionByLabel(
                user
        ).iterator().next();
        assertTrue(
                graphElementSearchResult.getContext().containsValue(
                        "vertex Cadeau"
                )
        );
    }
}
