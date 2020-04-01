/*
 * Copyright Vincent Blouin under the GPL License version 3
 */

package guru.bubl.test.module.model;

import guru.bubl.module.neo4j_graph_manipulator.graph.center_graph_element.CenterGraphElementsOperatorNeo4j;
import guru.bubl.test.module.model.center_graph_element.CenterGraphElementsOperatorTest;
import guru.bubl.test.module.model.center_graph_element.WholeGraphAdminTest;
import guru.bubl.test.module.model.graph.GraphElementOperatorTest;
import guru.bubl.test.module.model.graph.UserGraphTest;
import guru.bubl.test.module.model.graph.VertexOperatorTest;
import guru.bubl.test.module.utils.ModelTestRunner;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
@Ignore
@RunWith(Suite.class)
@Suite.SuiteClasses({
        CenterGraphElementsOperatorTest.class
})
public class ModelSpecificClassTest extends ModelTestRunner {}
