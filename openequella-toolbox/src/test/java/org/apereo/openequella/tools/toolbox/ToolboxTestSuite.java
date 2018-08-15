package org.apereo.openequella.tools.toolbox;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ConfigTest.class, ExportItemsDriverTest.class, FileUtilsTest.class, MigrationUtilsTest.class})
public class ToolboxTestSuite {

}
