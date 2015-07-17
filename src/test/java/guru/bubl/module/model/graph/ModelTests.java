/*
 * Copyright Vincent Blouin under the GPL License version 3
 */

package guru.bubl.module.model.graph;

import com.googlecode.junittoolbox.WildcardPatternSuite;
import guru.bubl.module.utils.ModelTestRunner;
import org.junit.runner.RunWith;
import com.googlecode.junittoolbox.SuiteClasses;

@RunWith(WildcardPatternSuite.class)
@SuiteClasses("**/*Test.class")
public class ModelTests extends ModelTestRunner {}
