/*
 * Copyright Vincent Blouin under the GPL License version 3
 */

package js_test_data.scenarios;

import com.google.gson.Gson;
import guru.bubl.module.model.ModelTestScenarios;
import guru.bubl.module.model.User;
import guru.bubl.module.model.admin.WholeGraphAdmin;
import guru.bubl.module.model.graph.GraphFactory;
import guru.bubl.module.model.graph.ShareLevel;
import guru.bubl.module.model.graph.relation.RelationOperator;
import guru.bubl.module.model.graph.subgraph.SubGraphJson;
import guru.bubl.module.model.graph.subgraph.SubGraphPojo;
import guru.bubl.module.model.graph.subgraph.UserGraph;
import guru.bubl.module.model.graph.vertex.VertexFactory;
import guru.bubl.module.model.graph.vertex.VertexOperator;
import guru.bubl.module.model.json.JsonUtils;
import guru.bubl.module.model.search.GraphElementSearchResult;
import guru.bubl.module.model.search.GraphSearchFactory;
import js_test_data.JsTestScenario;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.inject.Inject;
import java.util.List;


public class ThreeScenario implements JsTestScenario {

    /*
     b1{
        -r1->b2{
            ->
            ->
        }
        -r2->b3{
            -r3->-b4{
                ->
                ->
            },
            -r4->b5
        }
     }
     b2 is tagged to computer scientist
     b3 has the comment 'b3 comment'
     b3{
        <-r2-b1
        -r3->-b4{
            ->
            ->
        },
        -r4->b5
    }
    b4 and b5 are public
    Also single child scenario
    parent-relation->child
    graph where child is connected to parent.
     */

//    username has an accent


    @Inject
    GraphFactory graphFactory;

    @Inject
    VertexFactory vertexFactory;

    @Inject
    GraphSearchFactory graphSearchFactory;

    @Inject
    ModelTestScenarios modelTestScenarios;

    @Inject
    WholeGraphAdmin wholeGraphAdmin;

    User user = User.withEmailAndUsername(
            "a",
            "églantier"
    );

    User forkerUser = User.withEmailAndUsername("forker@example.com", "forker");

    private VertexOperator
            b1,
            b2,
            b21,
            b3,
            b4,
            b5,
            parent,
            child;

    private List<GraphElementSearchResult> forkedB1SearchResults;

    @Override
    public JSONObject build() {
        UserGraph userGraph = graphFactory.loadForUser(user);
        createVertices();
        createEdges();
        SubGraphPojo subGraphForB1 = userGraph.aroundForkUriInShareLevels(
                b1.uri(),
                ShareLevel.allShareLevelsInt
        );
        SubGraphPojo subGraphForB2 = userGraph.aroundForkUriInShareLevels(
                b2.uri(),
                ShareLevel.allShareLevelsInt
        );

        SubGraphPojo subGraphForB3 = userGraph.aroundForkUriInShareLevels(
                b3.uri(),
                ShareLevel.allShareLevelsInt
        );
        wholeGraphAdmin.reindexAll();
        List<GraphElementSearchResult> searchResultsForB1 = graphSearchFactory.usingSearchTerm(
                "b1"
        ).searchForAllOwnResources(
                user
        );
        List<GraphElementSearchResult> searchResultsForR2 = graphSearchFactory.usingSearchTerm(
                "r2"
        ).searchRelationsForAutoCompletionByLabel(
                user
        );
        SubGraphPojo subGraphForParent = userGraph.aroundForkUriInShareLevels(
                parent.uri(),
                ShareLevel.allShareLevelsInt
        );
        RelationOperator singleParentToB1Relation = parent.addRelationToFork(b1.uri(), parent.getShareLevel(), b1.getShareLevel());
        SubGraphPojo subGraphOfB1RelatedToParent = userGraph.aroundForkUriInShareLevels(
                b1.uri(),
                ShareLevel.allShareLevelsInt
        );
        singleParentToB1Relation.remove();
        child.mergeTo(b1);
        SubGraphPojo subGraphOfB1OnceMergedWithSingleChild = userGraph.aroundForkUriInShareLevels(
                b1.uri(),
                ShareLevel.allShareLevelsInt
        );
        b21.mergeTo(b3);
        SubGraphPojo b3SubGraphMergedWithB21 = userGraph.aroundForkUriInShareLevels(
                b3.uri(),
                ShareLevel.allShareLevelsInt
        );
        try {
            return new JSONObject().put(
                    "getGraph",
                    SubGraphJson.toJson(
                            subGraphForB1
                    )
            ).put(
                    "searchResultsForB1",
                    new JSONArray(
                            new Gson().toJson(searchResultsForB1)
                    )
            ).put(
                    "searchResultsForR2",
                    new JSONArray(
                            new Gson().toJson(searchResultsForR2)
                    )
            ).put(
                    "subGraphForB2",
                    SubGraphJson.toJson(
                            subGraphForB2
                    )
            ).put(
                    "subGraphForB3",
                    SubGraphJson.toJson(
                            subGraphForB3
                    )
            ).put(
                    "subGraphForParent",
                    SubGraphJson.toJson(
                            subGraphForParent
                    )
            ).put(
                    "subGraphOfB1RelatedToParent",
                    SubGraphJson.toJson(
                            subGraphOfB1RelatedToParent
                    )
            ).put(
                    "forkedB1SearchResults",
                    JsonUtils.getGson().toJson(
                            forkedB1SearchResults
                    )
            ).put(
                    "subGraphOfB1OnceMergedWithSingleChild",
                    SubGraphJson.toJson(
                            subGraphOfB1OnceMergedWithSingleChild
                    )
            ).put(
                    "b3SubGraphMergedWithB21",
                    SubGraphJson.toJson(
                            b3SubGraphMergedWithB21
                    )
            );
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void createVertices() {
        b1 = vertexFactory.createForOwner(
                user.username()
        );
        b1.label("b1");
        b2 = vertexFactory.createForOwner(
                user.username()
        );
        b2.label("b2");
        b21 = vertexFactory.createForOwner(
                user.username()
        );
        b21.label("b21");
        b2.addVertexAndRelation();
        b3 = vertexFactory.createForOwner(
                user.username()
        );
        b3.label("b3");
        b3.comment("b3 comment");
        b3.addTag(modelTestScenarios.computerScientistType(), ShareLevel.PRIVATE);
        b4 = vertexFactory.createForOwner(
                user.username()
        );
        b4.label("b4");
        b4.makePublic();
        b5 = vertexFactory.createForOwner(
                user.username()
        );
        b5.makePublic();
        b5.label("b5");
        parent = vertexFactory.createForOwner(
                user.username()
        );
        parent.label("parent");
        child = vertexFactory.createForOwner(
                user.username()
        );
        child.label("child");
    }

    private void createEdges() {
        b1.addRelationToFork(b2.uri(), b1.getShareLevel(), b2.getShareLevel()).label(
                "r1"
        );
        b2.addRelationToFork(b21.uri(), ShareLevel.PRIVATE, ShareLevel.PRIVATE).label(
                "rb21"
        );
        b1.addRelationToFork(b3.uri(), b1.getShareLevel(), b3.getShareLevel()).label(
                "r2"
        );
        b3.addRelationToFork(b4.uri(), b3.getShareLevel(), b4.getShareLevel()).label(
                "r3"
        );
        b3.addRelationToFork(b5.uri(), b3.getShareLevel(), b4.getShareLevel()).label(
                "r4"
        );
        b4.addVertexAndRelation();
        b4.addVertexAndRelation();
        parent.addRelationToFork(child.uri(), parent.getShareLevel(), child.getShareLevel()).label(
                "relation"
        );
    }
}
