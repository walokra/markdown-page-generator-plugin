package com.ruleoftech.markdown.page.generator.plugin;

import org.apache.commons.io.FileUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Resource;
import org.apache.maven.model.interpolation.MavenBuildTimestamp;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.filtering.*;
import org.codehaus.plexus.*;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.*;
import org.codehaus.plexus.util.StringUtils;
import org.pegdown.Extensions;
import org.pegdown.PegDownProcessor;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Creates a static html from markdown files.
 */
@Mojo(name = "generate")
public class MdPageGeneratorMojo extends AbstractMojo {

    @Parameter(property = "generate.defaultTitle")
    private String defaultTitle;

    @Parameter(property = "generate.alwaysUseDefaultTitle", defaultValue = "false")
    private boolean alwaysUseDefaultTitle;

    @Parameter(property = "generate.inputDirectory", defaultValue = "${project.basedir}/src/main/resources/markdown/")
    private String inputDirectory;

    @Parameter(property = "generate.outputDirectory", defaultValue = "${project.build.directory}/html/")
    private String outputDirectory;

    @Parameter(property = "generate.headerHtmlFile")
    private String headerHtmlFile;

    @Parameter(property = "generate.footerHtmlFile")
    private String footerHtmlFile;

    @Parameter(property = "generate.failIfFilesAreMissing", defaultValue = "true")
    private boolean failIfFilesAreMissing;

    @Parameter(property = "generate.recursiveInput", defaultValue = "false")
    private boolean recursiveInput;

    @Parameter(property = "generate.transformRelativeMarkdownLinks", defaultValue = "false")
    private boolean transformRelativeMarkdownLinks;

    @Parameter(property = "generate.inputEncoding", defaultValue = "${project.build.sourceEncoding}")
    private String inputEncoding;

    @Parameter(property = "generate.outputEncoding", defaultValue = "${project.build.sourceEncoding}")
    private String outputEncoding;

    @Parameter(property = "generate.parsingTimeoutInMillis")
    private Long parsingTimeoutInMillis;

    @Parameter(property = "generate.inputFileExtension", defaultValue = "md")
    private String inputFileExtension = "md";

    @Parameter(property = "generate.applyFiltering", defaultValue = "false")
    private boolean applyFiltering;

    @Parameter(property = "generate.timestampFormat", defaultValue = "yyyy-MM-dd\\'T\\'HH:mm:ss\\'Z\\'")
    private String timestampFormat;

    @Component
    private MavenProject project;

    /**
     * The maven session.
     */
    @Component
    protected MavenSession session;

    // Possible options
    // SMARTS: Beautifies apostrophes, ellipses ("..." and ". . .") and dashes ("--" and "---")
    // QUOTES: Beautifies single quotes, double quotes and double angle quotes (« and »)
    // ABBREVIATIONS: Abbreviations in the way of PHP Markdown Extra.
    // HARDWRAPS: Alternative handling of newlines, see Github-flavoured-Markdown
    // AUTOLINKS: Plain (undelimited) autolinks the way Github-flavoured-Markdown implements them.
    // TABLES: Tables similar to MultiMarkdown (which is in turn like the PHP Markdown Extra tables, but with colspan support).
    // DEFINITION LISTS: Definition lists in the way of PHP Markdown Extra.
    // FENCED CODE BLOCKS: Fenced Code Blocks in the way of PHP Markdown Extra or Github-flavoured-Markdown.
    // HTML BLOCK SUPPRESSION: Suppresses the output of HTML blocks.
    // INLINE HTML SUPPRESSION: Suppresses the output of inline HTML elements.
    // WIKILINKS Support [[Wiki-style links]] with a customizable URL rendering logic.
    @Parameter(property = "generate.pegdownExtensions", defaultValue = "TABLES")
    private String pegdownExtensions;

    private enum EPegdownExtensions {
        NONE(0x00),
        SMARTS(0x01),
        QUOTES(0x02),
        ABBREVIATIONS(0x04),
        HARDWRAPS(0x08),
        AUTOLINKS(0x10),
        TABLES(0x20),
        DEFINITIONS(0x40),
        FENCED_CODE_BLOCKS(0x80),
        WIKILINKS(0x100),
        ALL(0x0000FFFF),
        SUPPRESS_HTML_BLOCKS(0x00010000),
        SUPPRESS_INLINE_HTML(0x00020000),
        SUPPRESS_ALL_HTML(0x00030000),
        ANCHORLINKS(0x400);

        private final int value;

        EPegdownExtensions(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

    }

    /**
     * Comma separated string of directories to be copied.
     */
    @Parameter(property = "generate.copyDirectories")
    private String copyDirectories;

    private List<MarkdownDTO> markdownDTOs = new ArrayList<MarkdownDTO>();

    @Parameter(property = "generate.filteredOutputDirectory", defaultValue = "${project.build.directory}/filtered-md/")
    private File filteredOutputDiretory;

    /**
     * Execute the maven plugin.
     *
     * @throws MojoExecutionException Something went wrong
     */
    @Override
    public void execute() throws MojoExecutionException {
        // First, if filtering is enabled, perform that using the Maven magic
        if (applyFiltering) {
            peformMavenPropertyFiltering(new File(inputDirectory), filteredOutputDiretory, getInputEncoding());
            inputDirectory = filteredOutputDiretory.getAbsolutePath();
        }

        getLog().info("Pre-processing markdown files from input directory: " + inputDirectory);
        preprocessMarkdownFiles(new File(inputDirectory));

        if (!markdownDTOs.isEmpty()) {
            getLog().info("Process Pegdown extension options");
            int options = getPegdownExtensions(pegdownExtensions);

            getLog().info("Parse Markdown to HTML");
            processMarkdown(markdownDTOs, options);
        }

        // FIXME: This will possibly overwrite any filtering updates made in the maven property filtering step above
        if (StringUtils.isNotEmpty(copyDirectories)) {
            getLog().info("Copy files from directories");
            for (String dir : copyDirectories.split(",")) {
                copyFiles(inputDirectory + File.separator + dir, outputDirectory + File.separator + dir);
            }
        }
    }

    private int getPegdownExtensions(String extensions) {
        int options = 0;
        for (String ext : Arrays.asList(extensions.split("\\s*,\\s*"))) {
            try {
                if (!ext.isEmpty()) {
                    Field f = Extensions.class.getField(ext);
                    options |= f.getInt(null);
                }
            } catch (NoSuchFieldException e) {
                throw new IllegalArgumentException("No such extension: " + ext);
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException("Cannot read int value for extension " + ext + ": " + e, e);
            }
        }

        // getLog().info("Pegdown extension options = " + options);

        return options;
    }

    /**
     * Read Markdown files from directory.
     *
     * @return boolean
     * Is there files to read
     * @throws MojoExecutionException Unable to load file
     */
    private boolean preprocessMarkdownFiles(File inputDirectory) throws MojoExecutionException {
        getLog().debug("Read files from: " + inputDirectory);

        try {
            if (!inputDirectory.exists()) {
                getLog().info("There is no input folder for the project. Skipping.");
                return false;
            }
            int baseDepth = StringUtils.countMatches(inputDirectory.getAbsolutePath(), File.separator);

            // Reading just the markdown dir and sub dirs if recursive option set
            List<File> markdownFiles = getFilesAsArray(FileUtils.iterateFiles(inputDirectory, new String[]{inputFileExtension}, recursiveInput));

            for (File file : markdownFiles) {
                getLog().debug("File getName() " + file.getName());
                getLog().debug("File getAbsolutePath() " + file.getAbsolutePath());
                getLog().debug("File getPath() " + file.getPath());

                MarkdownDTO dto = new MarkdownDTO();
                dto.markdownFile = file;

                dto.folderDepth = StringUtils.countMatches(file.getAbsolutePath(), File.separator) - (baseDepth + 1);

                if (alwaysUseDefaultTitle) {
                    dto.title = defaultTitle;
                } else {
                    List<String> raw = FileUtils.readLines(file, getInputEncoding());
                    dto.title = getTitle(raw);
                }

                if (applyFiltering) {
                    for (String line : FileUtils.readLines(file, getInputEncoding())) {
                        if (isVariableLine(line)) {
                            String key = line.replaceAll("(^\\{)|(=.*)", "");
                            String value = line.replaceAll("(^\\{(.*?)=)|(\\}$)", "");
                            getLog().debug("Substitute: '" + key + "' -> '" + value + "'");
                            dto.substitutes.put(key, value);
                        }
                    }
                }

                dto.htmlFile = new File(
                        recursiveInput
                                ? outputDirectory + File.separator + file.getParentFile().getPath().substring(inputDirectory.getPath().length()) + File.separator + file.getName().replaceAll("." + inputFileExtension, ".html")
                                : outputDirectory + File.separator + file.getName().replaceAll("." + inputFileExtension, ".html")
                );

                getLog().debug("File htmlFile() " + dto.htmlFile);

                markdownDTOs.add(dto);
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to load file " + e.getMessage(), e);
        }

        return true;
    }

    /**
     * Going through list of DTOs and parsing the markdown into HTML.
     * Add header and footer to the big String.
     *
     * @throws MojoExecutionException Unable to write file
     */
    private void processMarkdown(List<MarkdownDTO> markdownDTOs, int options) throws MojoExecutionException {
        getLog().debug("Process Markdown");
        getLog().debug("inputEncoding: '" + getInputEncoding() + "', outputEncoding: '" + getOutputEncoding() + "'");
        getLog().debug("parsingTimeout: " + getParsingTimeoutInMillis() + " ms");
        getLog().debug("applyFiltering: " + applyFiltering);

        for (MarkdownDTO dto : markdownDTOs) {
            getLog().debug("dto: " + dto);

            try {
                String headerHtml = "";
                String footerHtml = "";

                try {
                    if (StringUtils.isNotEmpty(headerHtmlFile)) {
                        headerHtml = FileUtils.readFileToString(new File(headerHtmlFile), getInputEncoding());
                        headerHtml = addTitleToHtmlFile(headerHtml, dto.title);
                        headerHtml = replaceVariables(headerHtml, dto.substitutes);
                        headerHtml = updateRelativePaths(headerHtml, dto.folderDepth);
                    }
                    if (StringUtils.isNotEmpty(footerHtmlFile)) {
                        footerHtml = FileUtils.readFileToString(new File(footerHtmlFile), getInputEncoding());
                        footerHtml = replaceVariables(footerHtml, dto.substitutes);
                        footerHtml = updateRelativePaths(footerHtml, dto.folderDepth);
                    }
                } catch (FileNotFoundException e) {
                    if (failIfFilesAreMissing) {
                        throw e;
                    } else {
                        getLog().warn("header and/or footer file is missing.");
                        headerHtml = "";
                        footerHtml = "";
                    }
                } catch (Exception e) {
                    throw new MojoExecutionException("Error while processing header/footer: " + e.getMessage(), e);
                }

                String markdown = FileUtils.readFileToString(dto.markdownFile, getInputEncoding());
                markdown = replaceVariables(markdown, dto.substitutes);
                // getLog().debug(markdown);

                PegDownProcessor pegDownProcessor = new PegDownProcessor(options, getParsingTimeoutInMillis());
                String markdownAsHtml;
                if (transformRelativeMarkdownLinks) {
                    markdownAsHtml = pegDownProcessor.markdownToHtml(markdown, new MDToHTMLExpLinkRender(inputFileExtension));
                } else {
                    markdownAsHtml = pegDownProcessor.markdownToHtml(markdown);
                }
                StringBuilder data = new StringBuilder();
                data.append(headerHtml);
                data.append(markdownAsHtml);
                data.append(footerHtml);

                FileUtils.writeStringToFile(dto.htmlFile, data.toString(), getOutputEncoding());
            } catch (MojoExecutionException e) {
                throw e;
            } catch (IOException e) {
                getLog().error("Error : " + e.getMessage(), e);
                throw new MojoExecutionException("Unable to write file " + e.getMessage(), e);
            }
        }
    }

    private String getInputEncoding() {
        if (StringUtils.isBlank(inputEncoding)) {
            return Charset.defaultCharset().name();
        } else {
            return inputEncoding;
        }
    }

    private String getOutputEncoding() {
        if (StringUtils.isBlank(outputEncoding)) {
            return Charset.defaultCharset().name();
        } else {
            return outputEncoding;
        }
    }

    public String getInputFileExtension() {
        return inputFileExtension;
    }

    private long getParsingTimeoutInMillis() {
        if (parsingTimeoutInMillis != null) {
            return parsingTimeoutInMillis;
        }

        return PegDownProcessor.DEFAULT_MAX_PARSING_TIME;
    }

    /**
     * Get the first h1 for the title.
     *
     * @param raw The markdown as a list of strings
     * @return The first # h1 in the Markdown file
     */
    private String getTitle(List<String> raw) {
        if (raw == null) {
            return defaultTitle;
        }
        String previousLine = "";
        for (String line : raw) {
            line = line.trim();
            if (line.startsWith("#")) {
                line = line.replace("#", "");
                return line;
            }
            //Checking for Setext style headers.
            //Line is considered a match if it passes:
            //Starts with either = or -
            //It has the same number of characters as the previous line
            //It only contains - or = and nothing else.
            //
            //If there is a match we consider the previous line to be the title.
            if ((line.startsWith("=") && StringUtils.countMatches(line, "=") == previousLine.length() && line.matches("^=+$"))
                    || (line.startsWith("-") && StringUtils.countMatches(line, "-") == previousLine.length() && line.matches("^-+$"))) {
                return previousLine;
            }
            previousLine = line;
        }
        return defaultTitle;
    }

    /**
     * Adds the title to the html file.
     *
     * @param html  The HTML string
     * @param title The title
     */
    private String addTitleToHtmlFile(String html, String title) {
        if (html == null) {
            return html;
        }
        if (title != null) {
            getLog().debug("Setting the title in the HTML file to: " + title);
            return html.replaceFirst("titleToken", title);
        } else {
            getLog().debug("Title was null, setting the title in the HTML file to an empty string");
            return html.replaceFirst("titleToken", "");
        }
    }

    /**
     * Replace variables to the html file.
     *
     * @param initialContent
     * @param variables
     * @return the updated html
     */
    private String replaceVariables(String initialContent, Map<String, String> variables) {
        String newContent = initialContent;
        // Only apply substitution if filtering is enabled and there is actually something to
        // substitute, otherwise just return the original content.
        if (applyFiltering && newContent != null) {
            newContent = newContent.replaceAll("(\\{.*=.*\\}?)", "");
            if (variables != null) {
                for (Map.Entry<String, String> substitute : variables.entrySet()) {
                    newContent = newContent.replace("${" + substitute.getKey() + "}", substitute.getValue());
                }
            }
        }

        return newContent;
    }

    private static boolean isVariableLine(String line) {
        return line.matches("^\\{.*=.*\\}$");
    }


    /**
     * Update relative include paths corresponding to the markdown file's location in the folder structure.
     *
     * @param html        The HTML string
     * @param folderDepth Current markdown file's folder depth
     */
    private String updateRelativePaths(String html, int folderDepth) {
        if (html == null) {
            return html;
        }
        getLog().debug("Updating relative paths in html includes (css, js).");
        return html.replaceAll("##SITE_BASE##", getSiteBasePrefix(folderDepth));
    }

    /**
     * Calculates relative path to site's base for folder depth.
     *
     * @param folderDepth Current markdown file's folder depth
     */
    private String getSiteBasePrefix(int folderDepth) {
        String pathToBase = ".";
        while (folderDepth > 0) {
            pathToBase += "/..";
            folderDepth--;
        }
        return pathToBase;
    }

    /**
     * Copy files from one dir to another based on file extensions.
     *
     * @param fromDir the directory to copy from
     * @param toDir   the directory to copy to
     * @throws MojoExecutionException Unable to copy file
     */
    private void copyFiles(String fromDir, String toDir) throws MojoExecutionException {
        getLog().debug("fromDir=" + fromDir + "; toDir=" + toDir);
        try {
            File fromDirFile = new File(fromDir);
            if (fromDirFile.exists()) {
                Iterator<File> files = FileUtils.iterateFiles(new File(fromDir), null, false);
                while (files.hasNext()) {
                    File file = files.next();
                    if (file.exists()) {
                        FileUtils.copyFileToDirectory(file, new File(toDir));
                    } else {
                        getLog().error("File '" + file.getAbsolutePath() + "' does not exist. Skipping copy");
                    }
                }
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to copy file " + e.getMessage(), e);
        }
    }

    private List<File> getFilesAsArray(Iterator<File> iterator) {
        List<File> files = new ArrayList<File>();
        while (iterator.hasNext()) {
            files.add(iterator.next());
        }
        return files;
    }

    /**
     * Store information about markdown file.
     */
    private class MarkdownDTO {
        public String title;
        public Map<String, String> substitutes = new HashMap<String, String>();
        public File htmlFile;
        public File markdownFile;
        public int folderDepth = 0;
    }


    /**
     * MAVEN RESOURCE FILTERING: Heavily borrowed from Apache Maven ResourcesMojo (https://maven.apache.org/plugins/maven-resources-plugin)
     **/

    private List<MavenResourcesFiltering> mavenFilteringComponents = new ArrayList<MavenResourcesFiltering>();
    private PlexusContainer plexusContainer;
    private List<String> mavenFilteringHints;

    @Component(role = MavenResourcesFiltering.class, hint = "default")
    protected MavenResourcesFiltering mavenResourcesFiltering;

    @Parameter
    protected List<String> nonFilteredFileExtensions;

    /**
     * By default files like {@code .gitignore}, {@code .cvsignore} etc. are excluded which means they will not being
     * copied. If you need them for a particular reason you can do that by settings this to {@code false}. This means
     * all files like the following will be copied.
     * <ul>
     * <li>Misc: &#42;&#42;/&#42;~, &#42;&#42;/#&#42;#, &#42;&#42;/.#&#42;, &#42;&#42;/%&#42;%, &#42;&#42;/._&#42;</li>
     * <li>CVS: &#42;&#42;/CVS, &#42;&#42;/CVS/&#42;&#42;, &#42;&#42;/.cvsignore</li>
     * <li>RCS: &#42;&#42;/RCS, &#42;&#42;/RCS/&#42;&#42;</li>
     * <li>SCCS: &#42;&#42;/SCCS, &#42;&#42;/SCCS/&#42;&#42;</li>
     * <li>VSSercer: &#42;&#42;/vssver.scc</li>
     * <li>MKS: &#42;&#42;/project.pj</li>
     * <li>SVN: &#42;&#42;/.svn, &#42;&#42;/.svn/&#42;&#42;</li>
     * <li>GNU: &#42;&#42;/.arch-ids, &#42;&#42;/.arch-ids/&#42;&#42;</li>
     * <li>Bazaar: &#42;&#42;/.bzr, &#42;&#42;/.bzr/&#42;&#42;</li>
     * <li>SurroundSCM: &#42;&#42;/.MySCMServerInfo</li>
     * <li>Mac: &#42;&#42;/.DS_Store</li>
     * <li>Serena Dimension: &#42;&#42;/.metadata, &#42;&#42;/.metadata/&#42;&#42;</li>
     * <li>Mercurial: &#42;&#42;/.hg, &#42;&#42;/.hg/&#42;&#42;</li>
     * <li>GIT: &#42;&#42;/.git, &#42;&#42;/.gitignore, &#42;&#42;/.gitattributes, &#42;&#42;/.git/&#42;&#42;</li>
     * <li>Bitkeeper: &#42;&#42;/BitKeeper, &#42;&#42;/BitKeeper/&#42;&#42;, &#42;&#42;/ChangeSet,
     * &#42;&#42;/ChangeSet/&#42;&#42;</li>
     * <li>Darcs: &#42;&#42;/_darcs, &#42;&#42;/_darcs/&#42;&#42;, &#42;&#42;/.darcsrepo,
     * &#42;&#42;/.darcsrepo/&#42;&#42;&#42;&#42;/-darcs-backup&#42;, &#42;&#42;/.darcs-temp-mail
     * </ul>
     *
     * @since 3.0.0
     */
    @Parameter(defaultValue = "true")
    protected boolean addDefaultExcludes;

    public void contextualize(Context context) throws ContextException {
        plexusContainer = (PlexusContainer) context.get(PlexusConstants.PLEXUS_KEY);
    }

    private void peformMavenPropertyFiltering(final File inputDirectory, final File outputDirectory, final String inputEncoding) throws MojoExecutionException {
        try {
            List<String> combinedFilters = getCombinedFiltersList();

            List<Resource> resources = new ArrayList<Resource>();
            final Resource resource = new Resource();
            resource.setFiltering(true);
            resource.setDirectory(inputDirectory.getAbsolutePath());

            resources.add(resource);
            MavenResourcesExecution mavenResourcesExecution = new MavenResourcesExecution(resources, outputDirectory, project, this.inputEncoding, combinedFilters, Collections.<String>emptyList(), session);

            // mavenResourcesExecution.setEscapeWindowsPaths(escapeWindowsPaths);

            // never include project build filters in this call, since we've already accounted for the POM build filters
            // above, in getCombinedFiltersList().
            mavenResourcesExecution.setInjectProjectBuildFilters(false);

            // mavenResourcesExecution.setEscapeString(escapeString);
            mavenResourcesExecution.setOverwrite(true);
            // mavenResourcesExecution.setIncludeEmptyDirs(includeEmptyDirs);
            // mavenResourcesExecution.setSupportMultiLineFiltering(supportMultiLineFiltering);
            // mavenResourcesExecution.setFilterFilenames(fileNameFiltering);
            mavenResourcesExecution.setAddDefaultExcludes(addDefaultExcludes);

            // Handle subject of MRESOURCES-99
            Properties additionalProperties = addSeveralSpecialProperties();
            mavenResourcesExecution.setAdditionalProperties(additionalProperties);

            // if these are NOT set, just use the defaults, which are '${*}' and '@'.
            // mavenResourcesExecution.setDelimiters(delimiters, useDefaultDelimiters);

            if (nonFilteredFileExtensions != null) {
                mavenResourcesExecution.setNonFilteredFileExtensions(nonFilteredFileExtensions);
            }

            mavenResourcesFiltering.filterResources(mavenResourcesExecution);

            executeUserFilterComponents(mavenResourcesExecution);

            mavenResourcesExecution.getOutputDirectory();
        } catch (MavenFilteringException e) {
            throw new MojoExecutionException("Failure while processing/fitering markdown sources: " + e.getMessage(), e);
        }
    }

    /**
     * This solves https://issues.apache.org/jira/browse/MRESOURCES-99.<br/>
     * BUT:<br/>
     * This should be done different than defining those properties a second time, cause they have already being defined
     * in Maven Model Builder (package org.apache.maven.model.interpolation) via BuildTimestampValueSource. But those
     * can't be found in the context which can be got from the maven core.<br/>
     * A solution could be to put those values into the context by Maven core so they are accessible everywhere. (I'm
     * not sure if this is a good idea). Better ideas are always welcome.
     * <p>
     * The problem at the moment is that maven core handles usage of properties and replacements in
     * the model, but does not the resource filtering which needed some of the properties.
     *
     * @return the new instance with those properties.
     */
    private Properties addSeveralSpecialProperties() {
        String timeStamp = new MavenBuildTimestamp(new Date(), timestampFormat).formattedTimestamp();
        Properties additionalProperties = new Properties();
        additionalProperties.put("mdpagegenerator.timestamp", timeStamp);
        if (project.getBasedir() != null) {
            additionalProperties.put("project.baseUri", project.getBasedir().getAbsoluteFile().toURI().toString());
        }

        return additionalProperties;
    }

    /**
     * @param mavenResourcesExecution {@link MavenResourcesExecution}
     * @throws MojoExecutionException  in case of wrong lookup.
     * @throws MavenFilteringException in case of failure.
     * @since 2.5
     */
    protected void executeUserFilterComponents(MavenResourcesExecution mavenResourcesExecution)
            throws MojoExecutionException, MavenFilteringException {

        if (mavenFilteringHints != null) {
            for (String hint : mavenFilteringHints) {
                try {
                    // CHECKSTYLE_OFF: LineLength
                    mavenFilteringComponents.add((MavenResourcesFiltering) plexusContainer.lookup(MavenResourcesFiltering.class.getName(), hint));
                    // CHECKSTYLE_ON: LineLength
                } catch (ComponentLookupException e) {
                    throw new MojoExecutionException(e.getMessage(), e);
                }
            }
        } else {
            getLog().debug("no use filter components");
        }

        if (mavenFilteringComponents != null && !mavenFilteringComponents.isEmpty()) {
            getLog().debug("execute user filters");
            for (MavenResourcesFiltering filter : mavenFilteringComponents) {
                filter.filterResources(mavenResourcesExecution);
            }
        }
    }

    @Parameter
    protected List<String> filters;

    @Parameter(defaultValue = "true")
    protected boolean useBuildFilters;

    @Parameter(defaultValue = "${project.build.filters}", readonly = true)
    protected List<String> buildFilters;

    /**
     * @return The combined filters.
     */
    protected List<String> getCombinedFiltersList() {
        if (filters == null || filters.isEmpty()) {
            return useBuildFilters ? buildFilters : null;
        } else {
            List<String> result = new ArrayList<String>();

            if (useBuildFilters && buildFilters != null && !buildFilters.isEmpty()) {
                result.addAll(buildFilters);
            }

            result.addAll(filters);

            return result;
        }
    }

}
