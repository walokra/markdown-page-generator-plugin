package com.ruleoftech.markdown.page.generator.plugin;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;

import java.io.File;

/**
 * Unit test for {@link MdPageGeneratorMojo}
 */
public class MdPageGeneratorMojoTest
        extends AbstractMojoTestCase {

    public void testBasicProject()
            throws Exception {
        File pom = getTestFile("src/test/resources/basic-project/pom.xml");
        assertTrue(pom.exists());

        MdPageGeneratorMojo mdPageGeneratorMojo = (MdPageGeneratorMojo) lookupMojo("generate", pom);
        assertNotNull(mdPageGeneratorMojo);

        mdPageGeneratorMojo.execute();

        File generatedMarkdown = new File(getBasedir(), "/target/test-harness/basic-project/html/README.html");
        assertTrue(generatedMarkdown.exists());
    }

    public void testRecursiveProject()
            throws Exception {
        File pom = getTestFile("src/test/resources/recursive-project/pom.xml");
        assertTrue(pom.exists());

        MdPageGeneratorMojo mdPageGeneratorMojo = (MdPageGeneratorMojo) lookupMojo("generate", pom);
        assertNotNull(mdPageGeneratorMojo);

        mdPageGeneratorMojo.execute();

        File index = new File(getBasedir(), "/target/test-harness/recursive-project/html/index.html");
        assertTrue(index.exists());

        File page1 = new File(getBasedir(), "/target/test-harness/recursive-project/html/pages/page-1.html");
        assertTrue(page1.exists());

        File page2 = new File(getBasedir(), "/target/test-harness/recursive-project/html/pages/page-2.html");
        assertTrue(page2.exists());

        File page11 = new File(getBasedir(), "/target/test-harness/recursive-project/html/pages/embedded/page-1-1.html");
        assertTrue(page11.exists());
    }

}
