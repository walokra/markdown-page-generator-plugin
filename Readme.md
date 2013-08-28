# Markdown to HTML Page Generator Maven Plugin

Plugin creates static HTML pages with Maven and Markdown. Uses [pegdown](https://github.com/sirthias/pegdown) Markdown processor.

You can configure the input and output directories and which files to copy. You can also include custom header and footer and general title. 

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
* defaultTitle : if set the titleToken is replaced in every page. Otherwise the first h1 is used.

The output will be:
* target/html/name_of_file.html

## Configuration

Add the plugin to the pom file in your project:
	
		<build>
			<plugins>
				<plugin>
					<groupId>com.ruleoftech</groupId>
					<artifactId>markdown-page-generator-plugin</artifactId>
					<version>0.1-SNAPSHOT</version>
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
			<version>0.1-SNAPSHOT</version>
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
		