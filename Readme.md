# Markdown to HTML Page Generator Maven Plugin

Plugin creates static HTML pages with Maven and Markdown. Uses [pegdown](https://github.com/sirthias/pegdown) Markdown processor. The code is Open Source and under MIT license.

The plugin can be found in [Sonatype's OSS repository](https://oss.sonatype.org/content/groups/public/com/ruleoftech/markdown-page-generator-plugin/) and in [Central Repository](http://search.maven.org/).

Continuous integration:
[![Build Status](https://travis-ci.org/walokra/markdown-page-generator-plugin.svg?branch=master)](https://travis-ci.org/walokra/markdown-page-generator-plugin)

Plugin Information:

        <plugin>
            <groupId>com.ruleoftech</groupId>
            <artifactId>markdown-page-generator-plugin</artifactId>
            <version>1.0.0</version>
        </plugin>

You can configure the input and output directories, which files to copy and which pegdown options are used. You can also include custom header and footer and general title. 

Default configuration which can be overridden:

* inputDirectory : ${project.basedir}/src/main/resources/markdown/
* outputDirectory : ${project.build.directory}/html/

Configuration options:

* variables substitution
    
    Example:

	    header.html
	    
            <!DOCTYPE html>
            <html lang="${lang}">
            <head>
            </head>

	    page.md:
	    
            # Title
            {lang=en}


       will output in page.html:

            <!DOCTYPE html>
            <html lang="en">
            <head>
            </head>
            <h1>Title</h1>
           
* headerHtmlFile : Location of header HTML file as String, ${project.basedir}/src/main/resources/markdown/html/header.html
	
	Example:
	
		<!DOCTYPE html>
		<html lang="en">
		<head>
			<title>titleToken</title>
			<meta charset="utf-8" />
			<link rel="stylesheet" href="##SITE_BASE##/css/default.css">
		</head>
		
	Note: **##SITE_BASE##** will be translated to a relative path from the markdown file's directory to base directory. This is not necessary if *recursiveInput* configuration is false.

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
* inputEncoding: Charset-Name used for reading the md-input, default: ${project.build.sourceEncoding} or Default-Charset
* outputEncoding: Charset-Name used for writing the html-output, default: ${project.build.sourceEncoding} or Default-Charset
* parsingTimeoutInMillis: timeout for the Markdown parser, default is 2000 millis

The output will be:
* target/html/name_of_file.html

## Configuration

Add the plugin to the pom file in your project:
	
		<build>
			<plugins>
				<plugin>
					<groupId>com.ruleoftech</groupId>
					<artifactId>markdown-page-generator-plugin</artifactId>
					<version>1.0.0</version>
					<executions>
						<execution>
							<phase>process-resources</phase>
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
			<version>1.0.0</version>
			<executions>
				<execution>
					<phase>process-resources</phase>
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
			<version>1.0.0</version>
			<executions>
				<execution>
					<phase>process-resources</phase>
					<goals>
						<goal>generate</goal>
					</goals>
				</execution>
			</executions>
			<configuration>
				<pegdownExtensions>TABLES,FENCED_CODE_BLOCKS,AUTOLINKS</pegdownExtensions>
			</configuration>
		</plugin>

Input- and Output-Encoding can be specified by:

		<plugin>
			<groupId>com.ruleoftech</groupId>
			<artifactId>markdown-page-generator-plugin</artifactId>
			<version>1.0.0</version>
			<executions>
				<execution>
					<phase>process-resources</phase>
					<goals>
						<goal>generate</goal>
					</goals>
				</execution>
			</executions>
                <configuration>
                    <inputDirectory>${basedir}/src/test/resources/encoding-project/src/main/resources/markdown</inputDirectory>
                    <outputDirectory>${basedir}/target/test-harness/encoding-project/html</outputDirectory>
                    <inputEncoding>UTF-8</inputEncoding>
                    <outputEncoding>ISO-8859-15</outputEncoding>
                </configuration>
		</plugin>
