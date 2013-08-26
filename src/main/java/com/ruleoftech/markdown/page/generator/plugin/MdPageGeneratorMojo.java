package com.ruleoftech.markdown.page.generator.plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.StringUtils;

import com.petebevin.markdown.MarkdownProcessor;

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
	public void execute() throws MojoExecutionException {
		if (StringUtils.isNotEmpty(copyDirectories)) {
			getLog().info("Copy files from directories");
			for (String dir : copyDirectories.split(",")) {
				copyFiles(inputDirectory + File.separator + dir, outputDirectory + File.separator + dir);
			}
		}

		getLog().info("Read Markdown files from input directory");
		readFiles();

		getLog().info("Parse Markdown to HTML");
		processMarkdown(markdownDTOs);
	}

	/**
	 * Read Markdown files from directory.
	 * 
	 * @throws MojoExecutionException
	 *             Unable to load file
	 */
	private void readFiles() throws MojoExecutionException {
		getLog().debug("Read files from: " + inputDirectory);

		try {
			// Reading just the markdown dir and not sub dirs
			List<File> markdownFiles = getFilesAsArray(FileUtils.iterateFiles(new File(inputDirectory), new String[] { "md" }, false));

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

				File htmlFile = new File(outputDirectory + "/" + file.getName().replaceAll(".md", ".html"));
				dto.htmlFile = htmlFile;

				markdownDTOs.add(dto);
			}
		} catch (IOException e) {
			throw new MojoExecutionException("Unable to load file " + e.getMessage(), e);
		}
	}

	/**
	 * Going through list of DTOs and parsing the markdown into HTML.
	 * Add header and footer to the big String.
	 * 
	 * @throws MojoExecutionException
	 *             Unable to write file
	 */
	private void processMarkdown(List<MarkdownDTO> markdownDTOs) throws MojoExecutionException {
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

				String markdownAsHtml = new MarkdownProcessor().markdown(markdown);
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
			// get files but not from the sub dirs
			Iterator<File> files = FileUtils.iterateFiles(new File(fromDir), null, false);
			while (files.hasNext()) {
				File file = files.next();
				FileUtils.copyFileToDirectory(file, new File(toDir));
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
