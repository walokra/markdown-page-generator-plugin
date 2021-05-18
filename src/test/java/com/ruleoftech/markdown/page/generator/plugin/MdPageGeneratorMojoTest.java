package com.ruleoftech.markdown.page.generator.plugin;

import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Unit test for {@link MdPageGeneratorMojo}
 */
public class MdPageGeneratorMojoTest extends BetterAbstractMojoTestCase {

    @Test
    public void testEncoding() throws Exception {
        final String expectedGeneratedHTMLFile = "/target/test-harness/encoding-project/target/html/README.html";

        // ensure this java-File will not be affected by
        // encoding issues therefore escape umlaut-characters
        final String UMLAUTS
                = "Some Umlauts: "
                + "\u00f6" + // &ouml;
                "\u00e4" + // &auml;
                "\u00fc" + // &uuml;
                " "
                + "\u00d6" + // &Ouml;
                "\u00c4" + // &Auml;
                "\u00dc" + // &Uuml;
                " " + //
                "\u00df"; // &szlig;
        final String EURO
                = "Euro: "
                + "\u20ac"; // &euro;

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

    @Test
    public void testBasicProject() throws Exception {
        final String expectedGeneratedHTMLFile = "/target/test-harness/basic-project/target/html/README.html";

        File pom = getTestFile("src/test/resources/basic-project/pom.xml");
        assertTrue(pom.exists());

        MdPageGeneratorMojo mdPageGeneratorMojo = (MdPageGeneratorMojo) lookupConfiguredMojo(pom, "generate");
        assertNotNull(mdPageGeneratorMojo);

        mdPageGeneratorMojo.execute();

        File generatedMarkdown = new File(getBasedir(), expectedGeneratedHTMLFile);
        assertTrue("Expected HTML file does not exist: " + generatedMarkdown, generatedMarkdown.exists());
    }

    @Test
    public void testBasicProjectWithRenamedInOutDirectories() throws Exception {
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

    @Test
    public void testCustomInOutDirectories() throws Exception {
        final String expectedGeneratedHTMLFile = "/target/test-harness/custom-inout-directories-project/target/docs/README.html";
        final String notExpectedGeneratedHTMLFile = "/target/test-harness/custom-inout-directories-project/target/html/README.html";

        File pom = getTestFile("src/test/resources/custom-inout-directories-project/pom.xml");
        assertTrue(pom.exists());

        MdPageGeneratorMojo mdPageGeneratorMojo = (MdPageGeneratorMojo) lookupConfiguredMojo(pom, "generate");
        assertNotNull(mdPageGeneratorMojo);

        mdPageGeneratorMojo.execute();

        File generatedMarkdown = new File(getBasedir(), expectedGeneratedHTMLFile);
        assertTrue("Expected HTML file does not exist: " + generatedMarkdown, generatedMarkdown.exists());

        File notGeneratedMarkdown = new File(getBasedir(), notExpectedGeneratedHTMLFile);
        assertFalse("Unexpected HTML file exist: " + notGeneratedMarkdown, notGeneratedMarkdown.exists());
    }

    @Test
    public void testBasicProjectExtension() throws Exception {
        final String expectedGeneratedHTMLFile = "/target/test-harness/basic-project-extension/target/html/README.html";

        File pom = getTestFile("src/test/resources/basic-project-extension/pom.xml");
        assertTrue(pom.exists());

        MdPageGeneratorMojo mdPageGeneratorMojo = (MdPageGeneratorMojo) lookupConfiguredMojo(pom, "generate");
        assertNotNull(mdPageGeneratorMojo);
        assertTrue(Arrays.equals(new String[]{"md", "markdown"}, mdPageGeneratorMojo.getInputFileExtensions()));

        mdPageGeneratorMojo.execute();

        File generatedMarkdown = new File(getBasedir(), expectedGeneratedHTMLFile);
        assertTrue("Expected HTML file does not exist: " + generatedMarkdown, generatedMarkdown.exists());

        String markDown = FileUtils.readFileToString(generatedMarkdown, "ISO-8859-15");
        assertNotNull(markDown);
        assertTrue(markDown.contains("README.html"));
    }

    @Test
    public void testBasicProjectOutputExtension() throws Exception {
        final String expectedGeneratedHTMLFile = "/target/test-harness/basic-project-output-extension/target/html/README.php";

        File pom = getTestFile("src/test/resources/basic-project-output-extension/pom.xml");
        assertTrue(pom.exists());

        MdPageGeneratorMojo mdPageGeneratorMojo = (MdPageGeneratorMojo) lookupConfiguredMojo(pom, "generate");
        assertNotNull(mdPageGeneratorMojo);

        mdPageGeneratorMojo.execute();

        File generatedMarkdown = new File(getBasedir(), expectedGeneratedHTMLFile);
        assertTrue("Expected HTML file does not exist: " + generatedMarkdown, generatedMarkdown.exists());

        String markDown = FileUtils.readFileToString(generatedMarkdown, "ISO-8859-15");
        assertNotNull(markDown);
        // TODO: resulted HTML should have self links with output file extension
        //  assertTrue(markDown.contains("README." + mdPageGeneratorMojo.getOutputFileExtensions()));
    }

    @Test
    public void testBasicProjectFlexmarkOption() throws Exception {
        final String expectedGeneratedHTMLFile = "/target/test-harness/basic-project-flexmark-option/target/html/README.html";

        File pom = getTestFile("src/test/resources/basic-project-flexmark-option/pom.xml");
        assertTrue(pom.exists());

        MdPageGeneratorMojo mdPageGeneratorMojo = (MdPageGeneratorMojo) lookupConfiguredMojo(pom, "generate");
        assertNotNull(mdPageGeneratorMojo);

        mdPageGeneratorMojo.execute();

        File generatedMarkdown = new File(getBasedir(), expectedGeneratedHTMLFile);
        assertTrue("Expected HTML file does not exist: " + generatedMarkdown, generatedMarkdown.exists());

        String markDown = FileUtils.readFileToString(generatedMarkdown, "ISO-8859-15");
        assertNotNull(markDown);
        assertTrue(markDown.contains("<ol start=\"3\">"));
    }

    @Test
    public void testEmptyBasicProject() {
        File pom = getTestFile("src/test/resources/empty-basic-project/pom.xml");
        assertTrue(pom.exists());

        try {
            MdPageGeneratorMojo mdPageGeneratorMojo = (MdPageGeneratorMojo) lookupConfiguredMojo(pom, "generate");
            assertNotNull(mdPageGeneratorMojo);
            mdPageGeneratorMojo.execute();
        } catch (final Exception e) {
            assertTrue(e.toString(), false);
        }
    }

    @Test
    public void testCustomAttributes() throws Exception {
        final String expectedGeneratedHTMLFile = "/target/test-harness/custom-attributes/target/html/README.html";

        File pom = getTestFile("src/test/resources/custom-attributes/pom.xml");
        assertTrue(pom.exists());

        MdPageGeneratorMojo mdPageGeneratorMojo = (MdPageGeneratorMojo) lookupConfiguredMojo(pom, "generate");
        assertNotNull(mdPageGeneratorMojo);
        assertTrue(Arrays.equals(new String[]{"md"}, mdPageGeneratorMojo.getInputFileExtensions()));

        mdPageGeneratorMojo.execute();

        File generatedMarkdown = new File(getBasedir(), expectedGeneratedHTMLFile);
        assertTrue("Expected HTML file does not exist: " + generatedMarkdown, generatedMarkdown.exists());

        String markDown = FileUtils.readFileToString(generatedMarkdown, "ISO-8859-15");
        assertNotNull(markDown);
        assertEquals("<h1>Lorem ipsum</h1>\n"
                + "<table class=\"table table-striped\">\n"
                + "<thead>\n"
                + "<tr><th> header 1 </th><th> header 2 </th></tr>\n"
                + "</thead>\n"
                + "<tbody>\n"
                + "<tr><td> data 1   </td><td> data 2   </td></tr>\n"
                + "<tr><td> data 3   </td><td> data 3   </td></tr>\n"
                + "</tbody>\n"
                + "</table>\n"
                + "<ul>\n"
                + "<li>bullet item 1</li>\n"
                + "<li>bullet item 2</li>\n"
                + "</ul>\n"
                + "<p>Paragraph</p>\n"
                + "<ol>\n"
                + "<li style=\"color:red\">numbered item 1</li>\n"
                + "<li style=\"color:red\">numbered item 2</li>\n"
                + "</ol>\n"
                + "<blockquote class=\"red\">\n"
                + "<p>block quote paragraph text</p>\n"
                + "</blockquote>\n", markDown);
    }

    @Test
    public void testRecursiveProject() throws Exception {
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

    //public void testParsingTimeout()
    //        throws Exception {
    //    File pom = getTestFile("src/test/resources/timeout-project/pom.xml");
    //    assertTrue(pom.exists());
    //
    //    MdPageGeneratorMojo mdPageGeneratorMojo = (MdPageGeneratorMojo) lookupConfiguredMojo(pom, "generate");
    //
    //    assertNotNull(mdPageGeneratorMojo);
    //
    //    try {
    //        mdPageGeneratorMojo.execute();
    //        fail();
    //    } catch (Exception ex) {
    //        assertEquals(ParsingTimeoutException.class, ex.getCause().getClass());
    //    }
    //}

    @Test
    public void testSubstituteProject() throws Exception {
        final String expectedGeneratedHTMLFile = "/target/test-harness/substitute-project/target/html/README.html";

        File pom = getTestFile("src/test/resources/substitute-project/pom.xml");
        assertTrue(pom.exists());

        MdPageGeneratorMojo mdPageGeneratorMojo = (MdPageGeneratorMojo) lookupConfiguredMojo(pom, "generate");
        assertNotNull(mdPageGeneratorMojo);

        mdPageGeneratorMojo.execute();

        File generatedMarkdown = new File(getBasedir(), expectedGeneratedHTMLFile);
        assertTrue("Expected HTML file does not exist: " + generatedMarkdown, generatedMarkdown.exists());

        String html = FileUtils.readFileToString(generatedMarkdown, Charset.defaultCharset());

        assertFalse("Shouldn't contain the var declaration", html.contains("headerSubstitution"));
        assertFalse("Shouldn't contain the var declaration", html.contains("footerSubstitution"));

        assertFalse("Shouldn't contain the original var", html.contains("${headerSubstitution}"));
        assertFalse("Shouldn't contain the original var", html.contains("${footerSubstitution}"));

        assertTrue("Should contain the replaced var", html.contains("The new header"));
        assertTrue("Should contain the replaced var", html.contains("The new footer"));

        assertTrue("Should contain the replaced Maven variable 'substitute-project'", html.contains("substitute-project"));

        assertTrue("Should contain the original var 'beanId'", html.contains("${beanId}"));
        assertTrue("Should contain the original var 'cacheManager'", html.contains("${cacheManager}"));
    }

    @Test
    public void testCopiedFiles() throws MojoExecutionException, IOException {
        File sourceFolder = Files.createTempDir();
        try {
            File destinationFolder = Files.createTempDir();
            try {
                String[] folderNames = new String[]{"folder1", "folder2", "folder3"};
                String[] fileNames = new String[]{"file1", "file2", "file3"};
                createSubFoldersAndFiles(folderNames, fileNames, sourceFolder);

                printStructure("input", sourceFolder);

                MdPageGeneratorMojo mdPageGeneratorMojo = new MdPageGeneratorMojo();
                mdPageGeneratorMojo.setInputDirectory(sourceFolder.getAbsolutePath());
                mdPageGeneratorMojo.setOutputDirectory(destinationFolder.getAbsolutePath());
                mdPageGeneratorMojo.setCopyDirectories("folder1,folder3,folder4");

                mdPageGeneratorMojo.execute();

                printStructure("output", sourceFolder);

                Assert.assertTrue(getSubFolder(destinationFolder, "folder1").exists());
                Assert.assertFalse(getSubFolder(destinationFolder, "folder2").exists());
                Assert.assertTrue(getSubFolder(destinationFolder, "folder3").exists());
                Assert.assertFalse(getSubFolder(destinationFolder, "folder4").exists());

            } finally {
                deleteRecursively(destinationFolder);
            }
        } finally {
            deleteRecursively(sourceFolder);
        }
    }

    @Test
    public void testCopiedFilesWithWildcard() throws MojoExecutionException, IOException {
        File sourceFolder = Files.createTempDir();
        try {
            File destinationFolder = Files.createTempDir();
            try {
                String[] folderNames = new String[]{"folder1", "folder2", "folder3"};
                String[] fileNames = new String[]{"file1", "file2", "file3"};
                createSubFoldersAndFiles(folderNames, fileNames, sourceFolder);

                printStructure("input", sourceFolder);

                MdPageGeneratorMojo mdPageGeneratorMojo = new MdPageGeneratorMojo();
                mdPageGeneratorMojo.setInputDirectory(sourceFolder.getAbsolutePath());
                mdPageGeneratorMojo.setOutputDirectory(destinationFolder.getAbsolutePath());
                mdPageGeneratorMojo.setCopyDirectories("folder*");

                mdPageGeneratorMojo.execute();

                printStructure("output", destinationFolder);

                Assert.assertTrue(getSubFolder(destinationFolder, "folder1").exists());
                Assert.assertTrue(getSubFolder(destinationFolder, "folder2").exists());
                Assert.assertTrue(getSubFolder(destinationFolder, "folder3").exists());
                Assert.assertFalse(getSubFolder(destinationFolder, "folder4").exists());

            } finally {
                deleteRecursively(destinationFolder);
            }
        } finally {
            deleteRecursively(sourceFolder);
        }
    }

    @Test
    public void testCopiedFilesWithSubFoldersAndWildcard() throws MojoExecutionException, IOException {
        File sourceFolder = Files.createTempDir();
        try {
            File destinationFolder = Files.createTempDir();
            try {
                String[] folderNames = new String[]{"folder1", "folder2", "folder3"};
                String[] subFolderNames = new String[]{"folder1", "folder1", "folder1"};
                createImageSubFoldersAndFiles(folderNames, subFolderNames, sourceFolder);

                printStructure("input", sourceFolder);

                MdPageGeneratorMojo mdPageGeneratorMojo = new MdPageGeneratorMojo();
                mdPageGeneratorMojo.setInputDirectory(sourceFolder.getAbsolutePath());
                mdPageGeneratorMojo.setOutputDirectory(destinationFolder.getAbsolutePath());
                mdPageGeneratorMojo.setCopyDirectories("folder*/images");

                mdPageGeneratorMojo.execute();

                printStructure("output", destinationFolder);

                Assert.assertTrue(getSubFolder(destinationFolder, "folder1").exists());
                Assert.assertTrue(getSubFolder(destinationFolder, "folder2").exists());
                Assert.assertTrue(getSubFolder(destinationFolder, "folder3").exists());
                Assert.assertFalse(getSubFolder(destinationFolder, "folder4").exists());

                for (String folderName : new String[]{"folder1", "folder2", "folder3"}) {
                    final File subFolder = getSubFolder(destinationFolder, folderName);
                    Assert.assertTrue(subFolder.exists());

                    verifyImageFolder(subFolder, "images");
                }

            } finally {
                deleteRecursively(destinationFolder);
            }
        } finally {
            deleteRecursively(sourceFolder);
        }
    }

    @Test
    public void testCopiedFilesWithSubFoldersAndDoubleStarWildcard() throws MojoExecutionException, IOException {
        File sourceFolder = Files.createTempDir();
        try {
            File destinationFolder = Files.createTempDir();
            try {
                String[] folderNames = new String[]{"folder1", "folder2", "folder3"};
                String[] subFolderNames = new String[]{"folder1", "folder2", "folder3"};
                createImageSubFoldersAndFiles(folderNames, subFolderNames, sourceFolder);

                printStructure("input", sourceFolder);

                MdPageGeneratorMojo mdPageGeneratorMojo = new MdPageGeneratorMojo();
                mdPageGeneratorMojo.setInputDirectory(sourceFolder.getAbsolutePath());
                mdPageGeneratorMojo.setOutputDirectory(destinationFolder.getAbsolutePath());
                mdPageGeneratorMojo.setCopyDirectories("**/images");

                mdPageGeneratorMojo.execute();

                printStructure("output", destinationFolder);

                Assert.assertFalse(getSubFolder(destinationFolder, "folder4").exists());

                for (String folderName : new String[]{"folder1", "folder2", "folder3"}) {
                    final File subFolder = getSubFolder(destinationFolder, folderName);
                    Assert.assertTrue(subFolder.exists());

                    verifyImageFolder(subFolder, "images");

                    for (String subFolderName : new String[]{"folder1", "folder2", "folder3"}) {
                        final File subSubFolder = getSubFolder(subFolder, subFolderName);
                        Assert.assertTrue(subSubFolder.exists());

                        verifyImageFolder(subSubFolder, "images");
                    }
                }

            } finally {
                deleteRecursively(destinationFolder);
            }
        } finally {
            deleteRecursively(sourceFolder);
        }

    }

    @Test
    public void testFilenameProject() throws Exception {
        final String expectedGeneratedHTMLFile = "/target/test-harness/filename-project/target/html/stammdaten.html";

        File pom = getTestFile("src/test/resources/filename-project/pom.xml");
        assertTrue(pom.exists());

        MdPageGeneratorMojo mdPageGeneratorMojo = (MdPageGeneratorMojo) lookupConfiguredMojo(pom, "generate");
        assertNotNull(mdPageGeneratorMojo);

        mdPageGeneratorMojo.execute();

        File generatedMarkdown = new File(getBasedir(), expectedGeneratedHTMLFile);
        assertTrue("Expected HTML file does not exist: " + generatedMarkdown, generatedMarkdown.exists());
    }

    @Test
    public void testCodeBlockProject() throws Exception {
        final String expectedGeneratedHTMLFile = "/target/test-harness/codeblock-project/target/html/README.html";

        File pom = getTestFile("src/test/resources/codeblock-project/pom.xml");
        assertTrue(pom.exists());

        MdPageGeneratorMojo mdPageGeneratorMojo = (MdPageGeneratorMojo) lookupConfiguredMojo(pom, "generate");
        assertNotNull(mdPageGeneratorMojo);

        mdPageGeneratorMojo.execute();

        File generatedMarkdown = new File(getBasedir(), expectedGeneratedHTMLFile);
        assertTrue("Expected HTML file does not exist: " + generatedMarkdown, generatedMarkdown.exists());

      String html = FileUtils.readFileToString(generatedMarkdown, Charset.defaultCharset());

        assertEquals("<h1>Lorem ipsum</h1>\n"
                + "<pre><code>&lt;dependency&gt;\n"
                + "    &lt;groupId&gt;groupid&lt;/groupId&gt;\n"
                + "    &lt;artifactId&gt;artifactid&lt;/artifactId&gt;\n"
                + "    &lt;version&gt;version&lt;/version&gt;\n"
                + "&lt;/dependency&gt;\n"
                + "</code></pre>\n", html);
    }

    @Test
    public void testExampleProject() throws Exception {
        final String expectedGeneratedHTMLFile = "/target/test-harness/example-project/target/html/README.html";

        File pom = getTestFile("src/test/resources/example-project/pom.xml");
        assertTrue(pom.exists());

        MdPageGeneratorMojo mdPageGeneratorMojo = (MdPageGeneratorMojo) lookupConfiguredMojo(pom, "generate");
        assertNotNull(mdPageGeneratorMojo);

        mdPageGeneratorMojo.execute();

        File generatedMarkdown = new File(getBasedir(), expectedGeneratedHTMLFile);
        assertTrue("Expected HTML file does not exist: " + generatedMarkdown, generatedMarkdown.exists());
    }

    private void createSubFoldersAndFiles(String[] folderNames, String[] fileNames, File sourceFolder) throws IOException {
        for (String folderName : folderNames) {
            final File subFolder = getSubFolder(sourceFolder, folderName);
            subFolder.mkdir();
            for (String fileName : fileNames) {
                getSubFolder(subFolder, fileName).createNewFile();
            }
        }
    }

    private void createImageSubFoldersAndFiles(String[] folderNames, String[] subFolderNames, File sourceFolder) throws IOException {
        for (String folderName : folderNames) {
            final File subFolder = getSubFolder(sourceFolder, folderName);
            subFolder.mkdir();

            createImageFolderWithFiles(subFolder);

            for (String subFolderName : subFolderNames) {
                final File subSubFolder = getSubFolder(subFolder, subFolderName);

                subSubFolder.mkdir();
                createImageFolderWithFiles(subSubFolder);
            }
        }
    }

    private void verifyImageFolder(final File subFolder, String folderName) {
        final File imageFolder = getSubFolder(subFolder, folderName);
        Assert.assertTrue(imageFolder.exists());

        for (String fileName : new String[]{"file1", "file2", "file3"}) {
            Assert.assertTrue(getSubFolder(imageFolder, fileName).exists());
        }
    }

    private File createImageFolderWithFiles(final File subFolder) throws IOException {
        final File subSubFolder = getSubFolder(subFolder, "images");
        subSubFolder.mkdir();
        for (String fileName : new String[]{"file1", "file2", "file3"}) {
            getSubFolder(subSubFolder, fileName).createNewFile();
        }
        return subSubFolder;
    }

    private static File getSubFolder(File sourceFolder, final String folderName) {
        return sourceFolder.toPath().resolve(folderName).toFile();
    }

    private void deleteRecursively(File folder) {
        for (File toDelete : folder.listFiles()) {
            toDelete.delete();
        }
        folder.delete();
    }

    private void printStructure(String type, File folderToPrint) {
        System.out.println("file structure of '" + type + "'");
        Iterator<File> files = FileUtils.iterateFiles(folderToPrint, null, true);
        while (files.hasNext()) {
            File file = files.next();
            System.out.println(folderToPrint.toPath().relativize(file.toPath()));
        }
    }
}
