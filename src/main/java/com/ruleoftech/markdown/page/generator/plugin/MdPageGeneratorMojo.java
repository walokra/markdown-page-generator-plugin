package com.ruleoftech.markdown.page.generator.plugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.StringUtils;
import org.pegdown.Extensions;
import org.pegdown.PegDownProcessor;

/**
 * Creates a static html from markdown files.
 * 
 */
@Mojo(name = "generate")
public class MdPageGeneratorMojo extends AbstractMojo {

	@Parameter(property = "generate.defaultTitle")
	private String defaultTitle;

	@Parameter(property = "generate.inputDirectory", defaultValue = "${project.basedir}/src/main/resources/markdown/")
	private String inputDirectory;

	@Parameter(property = "generate.outputDirectory", defaultValue = "${project.build.directory}/html/")
	private String outputDirectory;

	@Parameter(property = "generate.headerHtmlFile")
	private String headerHtmlFile;

	@Parameter(property = "generate.footerHtmlFile")
	private String footerHtmlFile;

    @Parameter(property = "generate.recursiveInput", defaultValue = "false")
    private boolean recursiveInput;

    @Parameter(property = "generate.transformRelativeMarkdownLinks", defaultValue = "false")
    private boolean transformRelativeMarkdownLinks;
    
	// Possible options
	// SMARTS: Beautifies apostrophes, ellipses ("..." and ". . .") and dashes ("--" and "---")
	// QUOTES: Beautifies single quotes, double quotes and double angle quotes (« and »)
	// SMARTYPANTS: Convenience extension enabling both, SMARTS and QUOTES, at once.
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
		NONE(0x00), SMARTS(0x01), QUOTES(0x02), SMARTYPANTS(EPegdownExtensions.SMARTS.getValue() + EPegdownExtensions.QUOTES.getValue()), ABBREVIATIONS(
				0x04), HARDWRAPS(0x08), AUTOLINKS(0x10), TABLES(0x20), DEFINITIONS(0x40), FENCED_CODE_BLOCKS(0x80), WIKILINKS(0x100), ALL(
				0x0000FFFF), SUPPRESS_HTML_BLOCKS(0x00010000), SUPPRESS_INLINE_HTML(0x00020000), SUPPRESS_ALL_HTML(0x00030000);

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

	protected List<MarkdownDTO> markdownDTOs = new ArrayList<MarkdownDTO>();

	/**
	 * Execute the maven plugin.
	 * 
	 * @throws MojoExecutionException
	 *             Something went wrong
	 */
	@Override
	public void execute() throws MojoExecutionException {
		if (StringUtils.isNotEmpty(copyDirectories)) {
			getLog().info("Copy files from directories");
			for (String dir : copyDirectories.split(",")) {
				copyFiles(inputDirectory + File.separator + dir, outputDirectory + File.separator + dir);
			}
		}

		getLog().info("Read Markdown files from input directory");
		boolean hasFiles = readFiles();

        if (hasFiles) {
            getLog().info("Process Pegdown extension options");
            int options = getPegdownExtensions(pegdownExtensions);

            getLog().info("Parse Markdown to HTML");
            processMarkdown(markdownDTOs, options);
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
     *             Is there files to read
	 * @throws MojoExecutionException
	 *             Unable to load file
	 */
	private boolean readFiles() throws MojoExecutionException {
		getLog().debug("Read files from: " + inputDirectory);

		try {
			File inputFile = new File(inputDirectory);
			if (!inputFile.exists()) {
				getLog().info("There is no input folder for the project. Skipping.");
				return false;
			}
			 
			// Reading just the markdown dir and sub dirs if recursive option set
			List<File> markdownFiles = getFilesAsArray(FileUtils.iterateFiles(new File(inputDirectory), new String[] { "md" }, recursiveInput));

			for (File file : markdownFiles) {
				getLog().debug("File getName() " + file.getName());
				getLog().debug("File getAbsolutePath() " + file.getAbsolutePath());
				getLog().debug("File getPath() " + file.getPath());

				MarkdownDTO dto = new MarkdownDTO();
				dto.markdownFile = file;

				if (!StringUtils.isNotEmpty(defaultTitle)) {
					List<String> raw = FileUtils.readLines(file);
					dto.title = getTitle(raw);
				} else {
					dto.title = defaultTitle;
				}

                File htmlFile = new File(
                        recursiveInput
                                ? outputDirectory + "/" + file.getParentFile().getPath().substring(inputFile.getPath().length()) + "/" + file.getName().replaceAll(".md", ".html")
                                : outputDirectory + "/" + file.getName().replaceAll(".md", ".html")
                );
				dto.htmlFile = htmlFile;

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
	 * @throws MojoExecutionException
	 *             Unable to write file
	 */
	private void processMarkdown(List<MarkdownDTO> markdownDTOs, int options) throws MojoExecutionException {
		getLog().debug("processMarkdown");

		for (MarkdownDTO dto : markdownDTOs) {
			getLog().debug("dto: " + dto);

			try {
				String headerHtml = "";
				String footerHtml = "";
				if (StringUtils.isNotEmpty(headerHtmlFile)) {
					headerHtml = FileUtils.readFileToString(new File(headerHtmlFile));
					headerHtml = addTitleToHtmlFile(headerHtml, dto.title);
				}
				if (StringUtils.isNotEmpty(footerHtmlFile)) {
					footerHtml = FileUtils.readFileToString(new File(footerHtmlFile));
				}
				String markdown = FileUtils.readFileToString(dto.markdownFile);
				// getLog().debug(markdown);

				String markdownAsHtml;
				if (transformRelativeMarkdownLinks) {
					markdownAsHtml = new PegDownProcessor(options).markdownToHtml(markdown, new MDToHTMLExpLinkRender());
				} else {
					markdownAsHtml = new PegDownProcessor(options).markdownToHtml(markdown);
				}
				StringBuilder data = new StringBuilder();
				data.append(headerHtml);
				data.append(markdownAsHtml);
				data.append(footerHtml);

				FileUtils.writeStringToFile(dto.htmlFile, data.toString());
			} catch (IOException e) {
				getLog().error("Error : " + e.getMessage(), e);
				throw new MojoExecutionException("Unable to write file " + e.getMessage(), e);
			}
		}
	}

	/**
	 * Get the first h1 for the title.
	 * 
	 * @param raw
	 *            The markdown as a list of strings
	 * @return The first # h1 in the Markdown file
	 */
	private String getTitle(List<String> raw) {
		if (raw == null) {
			return defaultTitle;
		}
		for (String line : raw) {
			if (line.startsWith("#")) {
				line = line.replace("#", "");
				line = line.trim();
				return line;
			}
		}
		return defaultTitle;
	}

	/**
	 * Adds the title to the html file.
	 * 
	 * @param html
	 *            The HTML string
	 * @param title
	 *            The title
	 */
	private String addTitleToHtmlFile(String html, String title) {
		if (html == null) {
			return html;
		}
		getLog().debug("Setting the title in the HTML file to: " + title);
		return html.replaceFirst("titleToken", title);
	}

	/**
	 * Copy files from one dir to another based on file extensions.
	 * 
	 * @param fromDir
	 *            the directory to copy from
	 * @param toDir
	 *            the directory to copy to
	 * @throws MojoExecutionException
	 *             Unable to copy file
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
	 * 
	 */
	private class MarkdownDTO {
		public String title;
		public File htmlFile;
		public File markdownFile;
	}

}
