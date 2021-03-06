/*
 * Copyright Vincent Blouin under the GPL License version 3
 */

package js_test_data.scenarios;

import com.google.gson.Gson;
import guru.bubl.module.model.ModelTestScenarios;
import guru.bubl.module.model.graph.tag.TagFactory;
import guru.bubl.module.model.graph.tag.TagOperator;
import guru.bubl.module.model.search.GraphElementSearchResult;
import guru.bubl.module.model.search.GraphSearchFactory;
import guru.bubl.module.model.test.scenarios.TestScenarios;
import js_test_data.AbstractScenario;
import js_test_data.JsTestScenario;

import javax.inject.Inject;
import java.util.List;

public class TagsWithSameLabelSearchResultScenario extends AbstractScenario implements JsTestScenario {

    /*
     * meta0 nbReference 0
     * meta1 nbReference 1
     * meta2 nbReference 2
     * meta3 nbReference 3
     * meta4 nbReference 4
     */

    @Inject
    GraphSearchFactory graphSearchFactory;

    @Inject
    TagFactory tagFactory;

    @Inject
    ModelTestScenarios modelTestScenarios;

    TagOperator
            meta0,
            meta1,
            meta2,
            meta3;

    @Override
    public Object build() {
        createVertices();
        buildMetas();
        List<GraphElementSearchResult> searchResultsForMeta = graphSearchFactory.usingSearchTerm(
                "meta"
        ).searchForAllOwnResources(
                user
        );
        return new Gson().toJson(
                searchResultsForMeta
        );
    }

    private void buildMetas() {
        meta0 = tagFactory.withUri(
                center.addTag(
                        modelTestScenarios.person()
                ).values().iterator().next().uri());
        meta0.label("meta0");
        meta0.getNbNeighbors().setPrivate(0);
        meta1 = tagFactory.withUri(
                center.addTag(
                        TestScenarios.tagFromFriendlyResource(b1)
                ).values().iterator().next().uri());
        meta1.label("meta1");
        meta1.getNbNeighbors().setPrivate(1);
        meta2 = tagFactory.withUri(
                center.addTag(
                        TestScenarios.tagFromFriendlyResource(b2)
                ).values().iterator().next().uri());
        meta2.label("meta2");
        meta2.getNbNeighbors().setPrivate(2);
        meta3 = tagFactory.withUri(
                center.addTag(
                        TestScenarios.tagFromFriendlyResource(b3)
                ).values().iterator().next().uri());
        meta3.label("meta3");
        meta3.getNbNeighbors().setPrivate(3);
    }
}
