package com.ruleoftech.markdown.page.generator.plugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Resource;
import org.apache.maven.model.interpolation.MavenBuildTimestamp;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.filtering.MavenFilteringException;
import org.apache.maven.shared.filtering.MavenResourcesExecution;
import org.apache.maven.shared.filtering.MavenResourcesFiltering;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.util.StringUtils;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.profiles.pegdown.Extensions;
import com.vladsch.flexmark.profiles.pegdown.PegdownOptionsAdapter;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.builder.Extension;
import com.vladsch.flexmark.util.data.DataKey;
import com.vladsch.flexmark.util.data.MutableDataHolder;
import com.vladsch.flexmark.util.data.MutableDataSet;
import com.vladsch.flexmark.util.html.Attributes;

/**
 * Creates a static html from markdown files.
 */
@Mojo(name = "generate", threadSafe = true)
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

    @Parameter(property = "generate.inputFileExtensions", defaultValue = "md")
    private String inputFileExtensions = "md";

    @Parameter(property = "generate.outputFileExtension", defaultValue = "html")
    private String outputFileExtension = "html";

    @Parameter(property = "generate.applyFiltering", defaultValue = "false")
    private boolean applyFiltering;

    @Parameter(property = "generate.timestampFormat", defaultValue = "yyyy-MM-dd\\'T\\'HH:mm:ss\\'Z\\'")
    private String timestampFormat;

    @Parameter(property = "generate.attributes")
    private String[] attributes;

    @Component
    private MavenProject project;

    /**
     * The maven session.
     */
    @Component
    protected MavenSession session;

    @Parameter(property = "generate.pegdownExtensions", defaultValue = "TABLES")
    private String pegdownExtensions;

    @Parameter(property = "generate.flexmarkParserOptions")
    private String flexmarkParserOptions;

    /**
     * Comma separated string of directories to be copied.
     */
    @Parameter(property = "generate.copyDirectories")
    private String copyDirectories;

    private List<MarkdownDTO> markdownDTOs = new ArrayList<MarkdownDTO>();

    @Parameter(property = "generate.filteredOutputDirectory", defaultValue = "${project.build.directory}/filtered-md/")
    private File filteredOutputDirectory;

    public String getInputDirectory() {
        return inputDirectory;
    }

    public void setInputDirectory(String inputDirectory) {
        this.inputDirectory = inputDirectory;
    }

    public String getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public String getCopyDirectories() {
        return copyDirectories;
    }

    public void setCopyDirectories(String copyDirectories) {
        this.copyDirectories = copyDirectories;
    }

    /**
     * Execute the maven plugin.
     *
     * @throws MojoExecutionException Something went wrong
     */
    @Override
    public void execute() throws MojoExecutionException {
        // First, if filtering is enabled, perform that using the Maven magic
        if (applyFiltering) {
            performMavenPropertyFiltering(new File(inputDirectory), filteredOutputDirectory, getInputEncoding());
            inputDirectory = filteredOutputDirectory.getAbsolutePath();
        }

        getLog().info("Pre-processing markdown files from input directory: " + inputDirectory);
        if (!preprocessMarkdownFiles(new File(inputDirectory))){
			    getLog().info("Pre-processing markdown files from input directory: markdown files not found" + inputDirectory);
			    return;
		    }

        if (!markdownDTOs.isEmpty()) {
            getLog().info("Process Pegdown extension options");
            int pegdownOptions = getPegdownExtensions(pegdownExtensions);
            MutableDataHolder flexmarkOptions = getFlexmarkParserOptions(flexmarkParserOptions);
            final Map<String, Attributes> attributesMap = processAttributes(attributes);

            getLog().info("Parse Markdown to HTML");
            processMarkdown(markdownDTOs, pegdownOptions, flexmarkOptions, attributesMap);
        }

        // FIXME: This will possibly overwrite any filtering updates made in the maven property filtering step above
        if (StringUtils.isNotEmpty(copyDirectories)) {
            getLog().info("Copy files from directories");
            for (String dir : copyDirectories.split(",")) {
                for (Entry<String, String> copyAction : getFoldersToCopy(inputDirectory, outputDirectory, dir).entrySet()) {
                    copyFiles(copyAction.getKey(), copyAction.getValue());
                }
            }
        }
    }

    private Map<String, String> getFoldersToCopy(String inputDirectory, String outputDirectory, String dir) {
        Map<String, String> retValue = new HashMap<>();

        Collection<Path> stream = getPathMatchingGlob(inputDirectory, dir);
        for (Path path : stream) {
            final Path inFolderPath = new File(inputDirectory).toPath();
            Path relativePath = inFolderPath.relativize(path);

            Path resolvedOutPath = new File(outputDirectory).toPath().resolve(relativePath);
            Path resolvedInPath = new File(inputDirectory).toPath().resolve(relativePath);

            retValue.put(resolvedInPath.toFile().getAbsolutePath(), resolvedOutPath.toFile().getAbsolutePath());
        }

        return retValue;
    }

    private Collection<Path> getPathMatchingGlob(String inputDirectory1, String dir) {
        List<Path> retValue = new LinkedList<>();

        Iterator<File> files = FileUtils.iterateFiles(new File(inputDirectory1), null, true);
        while (files.hasNext()) {
            File file = files.next();
            file = file.getParentFile();

            if (file.isDirectory()) {

                String expandedGlob = new File(inputDirectory1).getAbsolutePath() + File.separator + dir;
                // we need other syntax on windows systems
                if (File.separator.equals("\\")) {
                    expandedGlob = expandedGlob.replaceAll("\\\\", "\\\\\\\\");
                }
                PathMatcher pathMatcher = file.toPath().getFileSystem().getPathMatcher("glob:" + expandedGlob);

                if (pathMatcher.matches(file.toPath())) {
                    if (!retValue.contains(file.toPath())) {
                        retValue.add(file.toPath());
                    }
                }
            }
        }

        return retValue;
    }


    /**
     * Parse attributes of the form NodeName:attributeName=attribute value:attributeName=attribute value...
     *
     * @param attributeList list of attributes
     * @return map of Node class to attributable part and attributes
     */
    private Map<String, Attributes> processAttributes(String[] attributeList) {
        Map<String, Attributes> nodeAttributeMap = new HashMap<>();

        for (String attribute : attributeList) {
            String[] nodeAttributes = attribute.split("\\|");
            Attributes attributes = new Attributes();
            for (int i = 1; i < nodeAttributes.length; i++) {
                String[] attributeNameValue = nodeAttributes[i].split("=", 2);
                if (attributeNameValue.length > 1) {
                    String value = attributeNameValue[1];
                    if (!value.isEmpty()) {
                        if (value.charAt(0) == '"' && value.charAt(value.length() - 1) == '"') {
                            value = value.substring(1, value.length() - 1);
                        } else if (value.charAt(0) == '\'' && value.charAt(value.length() - 1) == '\'') {
                            value = value.substring(1, value.length() - 1);
                        }
                    }
                    attributes.addValue(attributeNameValue[0], value);
                } else {
                    attributes.addValue(attributeNameValue[0], attributeNameValue[0]);
                }
            }
            nodeAttributeMap.put(nodeAttributes[0], attributes);
        }
        return nodeAttributeMap;
    }

    private int getPegdownExtensions(String extensions) {
    	if (extensions == null) {
            return 0;
        }

        int options = 0;
        for (String ext : Arrays.asList(extensions.split("\\s*,\\s*"))) {
            try {
                if (!ext.isEmpty()) {
                    Field f = Extensions.class.getField(ext);
                    options |= f.getInt(null);
                    getLog().info("Pegdown extension " + ext);
                }
            } catch (NoSuchFieldException e) {
                throw new IllegalArgumentException("No such extension: " + ext);
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException("Cannot read int value for extension " + ext + ": " + e, e);
            }
        }

        getLog().info("Pegdown extension options = " + options);

        return options;
    }

    private <T> MutableDataHolder getFlexmarkParserOptions(String options) {
        MutableDataHolder optionsAsDataKeys = new MutableDataSet();

    	if (options == null)
    		return optionsAsDataKeys;

        for (String opt : Arrays.asList(options.split("\\s*,\\s*"))) {
            try {
                Field f = Parser.class.getField(opt);
                @SuppressWarnings("unchecked")
				DataKey<T> dataKey = (DataKey<T>)f.get(null);
                optionsAsDataKeys.set(dataKey, dataKey.getDefaultValue(null));
                getLog().info("Flexmark option " + opt);
            } catch (NoSuchFieldException e) {
                throw new IllegalArgumentException("No such option: " + opt);
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException("Cannot read object value for extension " + opt + ": " + e, e);
            }
        }

        getLog().info("Flexmark option count = " + optionsAsDataKeys.getKeys().size());

        return optionsAsDataKeys;
    }

    /**
     * Read Markdown files from directory.
     *
     * @return boolean
     * Is there files to read
     * @throws MojoExecutionException Unable to load file
     */
    @SuppressWarnings("UnusedReturnValue")
    private boolean preprocessMarkdownFiles(File inputDirectory) throws MojoExecutionException {
        getLog().debug("Read files from: " + inputDirectory);

        try {
            if (!inputDirectory.exists()) {
                getLog().info("There is no input folder for the project. Skipping.");
                return false;
            }
            int baseDepth = StringUtils.countMatches(inputDirectory.getAbsolutePath(), File.separator);

            // Reading just the markdown dir and sub dirs if recursive option set
            List<File> markdownFiles = getFilesAsArray(FileUtils.iterateFiles(inputDirectory, getInputFileExtensions(), recursiveInput));

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
                            String value = line.replaceAll("(^\\{(.*?)=)|(}$)", "");
                            getLog().debug("Substitute: '" + key + "' -> '" + value + "'");
                            dto.substitutes.put(key, value);
                        }
                    }
                }

                String inputFileExtension = FilenameUtils.getExtension(file.getName());

                dto.htmlFile = new File(
                        recursiveInput
                                ? outputDirectory + File.separator
                                        + file.getParentFile().getPath().substring(inputDirectory.getPath().length())
                                        + File.separator + file.getName().replaceAll(
                                                "\\." + inputFileExtension,
                                                "\\." + outputFileExtension)
                                : outputDirectory + File.separator + file.getName().replaceAll("\\." + inputFileExtension,
                                        "\\." + outputFileExtension));

                getLog().debug("File htmlFile() " + dto.htmlFile);

                markdownDTOs.add(dto);
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to load file " + e.getMessage(), e);
        }

        return true;
    }

    /**
     * Replace variables with given pattern.
     *
     * @param template      String to replace
     * @param patternString regexp pattern
     * @param variables     variables to find
     * @return result
     */
    private String substituteVariables(String template, String patternString, Map<String, String> variables) {
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(template);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            if (variables.containsKey(matcher.group(1))) {
                String replacement = variables.get(matcher.group(1));
                // quote to work properly with $ and {,} signs
                matcher.appendReplacement(buffer, replacement != null ? Matcher.quoteReplacement(replacement) : "null");
            }
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    /**
     * Going through list of DTOs and parsing the markdown into HTML.
     * Add header and footer to the big String.
     *
     * @throws MojoExecutionException Unable to write file
     */
    private void processMarkdown(List<MarkdownDTO> markdownDTOs, int pegdownOptions, MutableDataHolder flexmarkOptions, final Map<String, Attributes> attributesMap) throws MojoExecutionException {
        getLog().debug("Process Markdown");
        getLog().debug("inputEncoding: '" + getInputEncoding() + "', outputEncoding: '" + getOutputEncoding() + "'");
        getLog().debug("applyFiltering: " + applyFiltering);

        MutableDataHolder finalFlexmarkOptions = PegdownOptionsAdapter.flexmarkOptions(pegdownOptions).toMutable();
        finalFlexmarkOptions.setAll(flexmarkOptions);
        List<Extension> extensions = new ArrayList<>();
        for (Extension extension : finalFlexmarkOptions.get(Parser.EXTENSIONS)) {
            extensions.add(extension);
        }

        if (transformRelativeMarkdownLinks) {
        	finalFlexmarkOptions.set(PageGeneratorExtension.INPUT_FILE_EXTENSIONS, inputFileExtensions);
            extensions.add(PageGeneratorExtension.create());
        }

        if (!attributesMap.isEmpty()) {
        	finalFlexmarkOptions.set(AttributesExtension.ATTRIBUTE_MAP, attributesMap);
            extensions.add(AttributesExtension.create());
        }

        finalFlexmarkOptions.set(Parser.EXTENSIONS, extensions);

        StringBuilder finalOptions = new StringBuilder("final flexmark options: ");
        for (DataKey<?> opt : finalFlexmarkOptions.keySet()) {
            finalOptions.append(opt.getName());
            finalOptions.append(" ");
        }
        getLog().debug(finalOptions.toString());
        
        Parser parser = Parser.builder(finalFlexmarkOptions).build();
        HtmlRenderer renderer = HtmlRenderer.builder(finalFlexmarkOptions).build();

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

                String markdownAsHtml;

                Node document = parser.parse(markdown);
                markdownAsHtml = renderer.render(document);

                String data = headerHtml + markdownAsHtml + footerHtml;
                FileUtils.writeStringToFile(dto.htmlFile, data, getOutputEncoding());
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

    public String[] getInputFileExtensions() {
        return inputFileExtensions.trim().split("\\s*,\\s*");
    }

    public String[] getOutputFileExtensions() {
        return outputFileExtension.trim().split("\\s*,\\s*");
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
     * Replace variables in the html file.
     *
     * @param initialContent html
     * @param variables      variable map
     * @return the updated html
     */
    private String replaceVariables(String initialContent, Map<String, String> variables) {
        String newContent = initialContent;
        // Only apply substitution if filtering is enabled and there is actually something to
        // substitute, otherwise just return the original content.
        if (applyFiltering && newContent != null) {
            newContent = newContent.replaceAll("\\{\\w*=.*}", "");
            if (variables != null) {
                newContent = substituteVariables(newContent, "\\$\\{(.+?)\\}", variables);
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
        List<File> files = new ArrayList<>();
        while (iterator.hasNext()) {
            files.add(iterator.next());
        }
        return files;
    }

    /**
     * Store information about markdown file.
     */
    private class MarkdownDTO {
        String title;
        Map<String, String> substitutes = new HashMap<>();
        File htmlFile;
        File markdownFile;
        int folderDepth = 0;
    }

    /**
     * MAVEN RESOURCE FILTERING: Heavily borrowed from Apache Maven ResourcesMojo (https://maven.apache.org/plugins/maven-resources-plugin)
     **/

    private List<MavenResourcesFiltering> mavenFilteringComponents = new ArrayList<>();
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

    private void performMavenPropertyFiltering(final File inputDirectory, final File outputDirectory, final String inputEncoding) throws MojoExecutionException {
        try {
            List<String> combinedFilters = getCombinedFiltersList();

            List<Resource> resources = new ArrayList<>();
            final Resource resource = new Resource();
            resource.setFiltering(true);
            resource.setDirectory(inputDirectory.getAbsolutePath());

            resources.add(resource);
            MavenResourcesExecution mavenResourcesExecution = new MavenResourcesExecution(
                    resources,
                    outputDirectory,
                    project,
                    this.inputEncoding,
                    combinedFilters,
                    Collections.<String>emptyList(),
                    session
            );

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
            throw new MojoExecutionException("Failure while processing/filtering markdown sources: " + e.getMessage(), e);
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
