package com.ruleoftech.markdown.page.generator.plugin;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.pegdown.ParsingTimeoutException;

import java.io.File;

/**
 * Unit test for {@link MdPageGeneratorMojo}
 */
public class MdPageGeneratorMojoTest
        extends AbstractMojoTestCase {

    public void testEncoding() throws Exception {

        // ensure this java-File will not be affected by
        // encoding issues therefore escape umlaut-characters
        final String UMLAUTS =
            "Some Umlauts: " +
            "\u00f6" + // &ouml;
            "\u00e4" + // &auml;
            "\u00fc" + // &uuml;
            " " +
            "\u00d6" + // &Ouml;
            "\u00c4" + // &Auml;
            "\u00dc" + // &Uuml;
            " " + //
            "\u00df"; // &szlig;
        final String EURO =
            "Euro: " +
            "\u20ac"; // &euro;

        File pom = getTestFile("src/test/resources/encoding-project/pom.xml");
        assertTrue(pom.exists());

        MdPageGeneratorMojo mdPageGeneratorMojo = (MdPageGeneratorMojo) lookupMojo("generate", pom);
        assertNotNull(mdPageGeneratorMojo);

        mdPageGeneratorMojo.execute();

        File generatedMarkdown = new File(getBasedir(), "/target/test-harness/encoding-project/html/README.html");
        assertNotNull(generatedMarkdown);
        assertTrue(generatedMarkdown.exists());

        String markDown = FileUtils.readFileToString(generatedMarkdown, "ISO-8859-15");
        assertNotNull(markDown);
        assertTrue(markDown.contains(UMLAUTS));
        assertTrue(markDown.contains(EURO));
    }

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
    
    public void testBasicProjectExtension()
            throws Exception {
        File pom = getTestFile("src/test/resources/basic-project-extension/pom.xml");
        assertTrue(pom.exists());

        MdPageGeneratorMojo mdPageGeneratorMojo = (MdPageGeneratorMojo) lookupMojo("generate", pom);
        assertNotNull(mdPageGeneratorMojo);
        assertEquals("markdown", mdPageGeneratorMojo.getInputFileExtension());

        mdPageGeneratorMojo.execute();

        File generatedMarkdown = new File(getBasedir(), "/target/test-harness/basic-project-extension/html/README.html");
        assertTrue(generatedMarkdown.exists());
        
        String markDown = FileUtils.readFileToString(generatedMarkdown, "ISO-8859-15");
        assertNotNull(markDown);
        assertTrue(markDown.contains("README.html"));
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

    public void testParsingTimeout()
            throws Exception {
        File pom = getTestFile("src/test/resources/timeout-project/pom.xml");
        assertTrue(pom.exists());

        MdPageGeneratorMojo mdPageGeneratorMojo = (MdPageGeneratorMojo) lookupMojo("generate", pom);
        assertNotNull(mdPageGeneratorMojo);

        try {
            mdPageGeneratorMojo.execute();
            fail();
        } catch (Exception ex) {
            assertEquals(ParsingTimeoutException.class, ex.getCause().getClass());
        }
    }

}
