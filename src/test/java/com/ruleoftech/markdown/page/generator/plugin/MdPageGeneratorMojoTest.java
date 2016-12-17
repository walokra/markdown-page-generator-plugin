package com.ruleoftech.markdown.page.generator.plugin;

import org.apache.commons.io.FileUtils;
import org.apache.maven.execution.*;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.project.*;
import org.pegdown.ParsingTimeoutException;

import java.io.File;

/**
 * Unit test for {@link MdPageGeneratorMojo}
 */
public class MdPageGeneratorMojoTest extends BetterAbstractMojoTestCase
{
    public void testEncoding() throws Exception
    {
        final String expectedGeneratedHTMLFile = "/target/test-harness/encoding-project/target/html/README.html";

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

        MdPageGeneratorMojo mdPageGeneratorMojo = (MdPageGeneratorMojo) lookupConfiguredMojo(pom, "generate");
        assertNotNull(mdPageGeneratorMojo);

        mdPageGeneratorMojo.execute();

        File generatedMarkdown = new File(getBasedir(), expectedGeneratedHTMLFile);
        assertTrue("Expected HTML file does not exist: " + generatedMarkdown, generatedMarkdown.exists());

        String markDown = FileUtils.readFileToString(generatedMarkdown, "ISO-8859-15");
        assertNotNull(markDown);
        assertTrue(markDown.contains(UMLAUTS));
        assertTrue(markDown.contains(EURO));

    }

    public void testBasicProject()
            throws Exception
    {
        final String expectedGeneratedHTMLFile = "/target/test-harness/basic-project/target/html/README.html";

        File pom = getTestFile("src/test/resources/basic-project/pom.xml");
        assertTrue(pom.exists());

        MdPageGeneratorMojo mdPageGeneratorMojo = (MdPageGeneratorMojo) lookupConfiguredMojo(pom, "generate");
        assertNotNull(mdPageGeneratorMojo);

        mdPageGeneratorMojo.execute();

        File generatedMarkdown = new File(getBasedir(), expectedGeneratedHTMLFile);
        assertTrue("Expected HTML file does not exist: " + generatedMarkdown, generatedMarkdown.exists());
    }

    public void testBasicProjectWithRenamedInOutDirectories() throws Exception
    {
        final String expectedGeneratedHTMLFile = "/target/test-harness/basic-project-custom-inout-directories/target/html-renamed/README.html";
        final String notExpectedGeneratedHTMLFile = "/target/test-harness/basic-project-custom-inout-directories/target/html/README.html";

        File pom = getTestFile("src/test/resources/basic-project-custom-inout-directories/pom.xml");
        assertTrue(pom.exists());

        MdPageGeneratorMojo mdPageGeneratorMojo = (MdPageGeneratorMojo) lookupConfiguredMojo(pom, "generate");
        assertNotNull(mdPageGeneratorMojo);

        mdPageGeneratorMojo.execute();

        File generatedMarkdown = new File(getBasedir(), expectedGeneratedHTMLFile);
        assertTrue("Expected HTML file does not exist: " + generatedMarkdown, generatedMarkdown.exists());

        File notGeneratedMarkdown = new File(getBasedir(), notExpectedGeneratedHTMLFile);
        assertFalse("Unexpected HTML file exist: " + notGeneratedMarkdown, notGeneratedMarkdown.exists());

    }

    public void testBasicProjectExtension()
            throws Exception
    {
        final String expectedGeneratedHTMLFile = "/target/test-harness/basic-project-extension/target/html/README.html";

        File pom = getTestFile("src/test/resources/basic-project-extension/pom.xml");
        assertTrue(pom.exists());

        MdPageGeneratorMojo mdPageGeneratorMojo = (MdPageGeneratorMojo) lookupConfiguredMojo(pom, "generate");
        assertNotNull(mdPageGeneratorMojo);
        assertEquals("markdown", mdPageGeneratorMojo.getInputFileExtension());

        mdPageGeneratorMojo.execute();

        File generatedMarkdown = new File(getBasedir(), expectedGeneratedHTMLFile);
        assertTrue("Expected HTML file does not exist: " + generatedMarkdown, generatedMarkdown.exists());

        String markDown = FileUtils.readFileToString(generatedMarkdown, "ISO-8859-15");
        assertNotNull(markDown);
        assertTrue(markDown.contains("README.html"));
    }

    public void testRecursiveProject()
            throws Exception
    {
        final String expectedGeneratedHTMLFileBaseDir = "/target/test-harness/recursive-project/target/html/";

        File pom = getTestFile("src/test/resources/recursive-project/pom.xml");
        assertTrue(pom.exists());

        MdPageGeneratorMojo mdPageGeneratorMojo = (MdPageGeneratorMojo) lookupConfiguredMojo(pom, "generate");
        assertNotNull(mdPageGeneratorMojo);

        mdPageGeneratorMojo.execute();

        File index = new File(getBasedir(), expectedGeneratedHTMLFileBaseDir + "index.html");
        assertTrue(index.exists());

        File page1 = new File(getBasedir(), expectedGeneratedHTMLFileBaseDir + "pages/page-1.html");
        assertTrue(page1.exists());

        File page2 = new File(getBasedir(), expectedGeneratedHTMLFileBaseDir + "pages/page-2.html");
        assertTrue(page2.exists());

        File page11 = new File(getBasedir(), expectedGeneratedHTMLFileBaseDir + "pages/embedded/page-1-1.html");
        assertTrue(page11.exists());
    }

    public void testParsingTimeout()
            throws Exception
    {
        File pom = getTestFile("src/test/resources/timeout-project/pom.xml");
        assertTrue(pom.exists());

        MdPageGeneratorMojo mdPageGeneratorMojo = (MdPageGeneratorMojo) lookupConfiguredMojo(pom, "generate");

        assertNotNull(mdPageGeneratorMojo);

        try
        {
            mdPageGeneratorMojo.execute();
            fail();
        }
        catch (Exception ex)
        {
            assertEquals(ParsingTimeoutException.class, ex.getCause().getClass());
        }
    }

    public void testSubstituteProject() throws Exception
    {
        final String expectedGeneratedHTMLFile = "/target/test-harness/substitute-project/target/html/README.html";

        File pom = getTestFile("src/test/resources/substitute-project/pom.xml");
        assertTrue(pom.exists());

        MdPageGeneratorMojo mdPageGeneratorMojo = (MdPageGeneratorMojo) lookupConfiguredMojo(pom, "generate");
        assertNotNull(mdPageGeneratorMojo);

        mdPageGeneratorMojo.execute();

        File generatedMarkdown = new File(getBasedir(), expectedGeneratedHTMLFile);
        assertTrue("Expected HTML file does not exist: " + generatedMarkdown, generatedMarkdown.exists());

        String html = FileUtils.readFileToString(generatedMarkdown);

        assertFalse("Shouldn't contains the var declaration", html.contains("headerSubstitution"));
        assertFalse("Shouldn't contains the var declaration", html.contains("footerSubstitution"));

        assertFalse("Shouldn't contains the original var", html.contains("${headerSubstitution}"));
        assertFalse("Shouldn't contains the original var", html.contains("${footerSubstitution}"));

        assertTrue("Should contains the replaced var", html.contains("The new header"));
        assertTrue("Should contains the replaced var", html.contains("The new footer"));

        assertTrue("Should contain the replaced Maven variable 'substitute-project'", html.contains("substitute-project"));

    }


}
