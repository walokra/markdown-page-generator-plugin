package com.ruleoftech.markdown.page.generator.plugin;

import java.io.File;

public class EmptyResourceMojoTest extends BetterAbstractMojoTestCase {
    public void testBasicProject() {

        final File pom = getTestFile("src/test/resources/empty-basic-project/pom.xml");
        assertTrue(pom.exists());

        try {
            final MdPageGeneratorMojo mdPageGeneratorMojo = (MdPageGeneratorMojo) lookupConfiguredMojo(pom, "generate");
            assertNotNull(mdPageGeneratorMojo);
            mdPageGeneratorMojo.execute();
        } catch (final Exception e) {
            assertTrue(e.toString(), false);
        }

    }
}
