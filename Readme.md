# Markdown to HTML Page Generator Maven Plugin

Plugin creates static HTML pages with Maven and Markdown. Uses [pegdown](https://github.com/sirthias/pegdown) Markdown processor. The code is Open Source and under MIT license.

The plugin can be found in [Sonatype's OSS repository](https://oss.sonatype.org/content/groups/public/com/ruleoftech/markdown-page-generator-plugin/) and in [Central Repository](http://search.maven.org/).

Continuous integration:
[![Build Status](https://travis-ci.org/walokra/markdown-page-generator-plugin.svg?branch=master)](https://travis-ci.org/walokra/markdown-page-generator-plugin)

Dependency Information:

        <dependency>
            <groupId>com.ruleoftech</groupId>
            <artifactId>markdown-page-generator-plugin</artifactId>
            <version>0.4</version>
        </dependency>

You can configure the input and output directories, which files to copy and which pegdown options are used. You can also include custom header and footer and general title. 

Default configuration which can be overridden:

* inputDirectory : ${project.basedir}/src/main/resources/markdown/
* outputDirectory : ${project.build.directory}/html/

Configuration options:

* headerHtmlFile : Location of header HTML file as String, ${project.basedir}/src/main/resources/markdown/html/header.html
	
	Example:
	
		<!DOCTYPE html>
		<html lang="en">
		<head>
			<title>titleToken</title>
			<meta charset="utf-8" />
		</head>
		
* footerHtmlFile : Location of header HTML file as String, ${project.basedir}/src/main/resources/markdown/html/footer.html
	
	Example:
	
		<footer>
		</footer>
		</html>

* copyDirectories :	Comma separead list of directories to copy to output directory, like: css,js,images
* defaultTitle : If set the titleToken is replaced in every page. Otherwise the first h1 is used.
* recursiveInput : Process also inputDirectory's sub directories if option true. Default false.
* transformRelativeMarkdownLinks : Transform relative url suffix from ".md" to ".html" if option true. Default false.
* pegdownExtensions: Comma separated list of constants as specified in org.pegdown.Extensions. The default is TABLES.

The output will be:
* target/html/name_of_file.html

## Configuration

Add the plugin to the pom file in your project:
	
		<build>
			<plugins>
				<plugin>
					<groupId>com.ruleoftech</groupId>
					<artifactId>markdown-page-generator-plugin</artifactId>
					<version>0.4</version>
					<executions>
						<execution>
							<phase>process-sources</phase>
							<goals>
								<goal>generate</goal>
							</goals>
						</execution>
					</executions>
				</plugin>
			</plugins>
		</build>

Or with custom header and footer:

		<plugin>
			<groupId>com.ruleoftech</groupId>
			<artifactId>markdown-page-generator-plugin</artifactId>
			<version>0.4</version>
			<executions>
				<execution>
					<phase>process-sources</phase>
					<goals>
						<goal>generate</goal>
					</goals>
				</execution>
			</executions>
			<configuration>
				<headerHtmlFile>${project.basedir}/src/main/resources/markdown/html/header.html</headerHtmlFile>
				<footerHtmlFile>${project.basedir}/src/main/resources/markdown/html/footer.html</footerHtmlFile>
				<copyDirectories>css,js</copyDirectories>
			</configuration>
		</plugin>
		
You can also specify the Pegdown extensions:  

		<plugin>
			<groupId>com.ruleoftech</groupId>
			<artifactId>markdown-page-generator-plugin</artifactId>
			<version>0.4</version>
			<executions>
				<execution>
					<phase>process-sources</phase>
					<goals>
						<goal>generate</goal>
					</goals>
				</execution>
			</executions>
			<configuration>
				<pegdownExtensions>TABLES,FENCED_CODE_BLOCKS,AUTOLINKS</pegdownExtensions>
			</configuration>
		</plugin>
