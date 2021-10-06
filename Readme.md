# Markdown to HTML Page Generator Maven Plugin

Plugin creates static HTML pages with Maven and Markdown. Uses [flexmark-java] Markdown processor. The code is Open Source and under MIT license.

[![CircleCI](https://circleci.com/gh/walokra/markdown-page-generator-plugin.svg?style=svg)](https://circleci.com/gh/walokra/markdown-page-generator-plugin)

[![Maven Central status](https://img.shields.io/maven-central/v/com.ruleoftech/markdown-page-generator-plugin.svg)](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.ruleoftech%22%20AND%20a%3A%22markdown-page-generator-plugin%22)

Plugin Information:

```xml
<plugin>
    <groupId>com.ruleoftech</groupId>
    <artifactId>markdown-page-generator-plugin</artifactId>
    <version>2.4.0</version>
</plugin>
```

Artifacts:
* [Snapshots](https://oss.sonatype.org/content/repositories/snapshots/com/ruleoftech/markdown-page-generator-plugin/)
* [Releases](https://oss.sonatype.org/content/repositories/releases/com/ruleoftech/markdown-page-generator-plugin/)

You can configure the input and output directories, which files to copy and which pegdown
options are used. You can also include custom header and footer and general title.

Default configuration which can be overridden:

* `inputDirectory`: `${project.basedir}/src/main/resources/markdown/`
* `outputDirectory`: `${project.build.directory}/html/`

Configuration options:

* `applyFiltering`: apply variables substitution if `true`. Default `false`.

  Example:

  `header.html`:

  ```html
  <!DOCTYPE html>

  <html lang="${lang}">
  <head>
  </head>
  ```

  `page.md`:

  ```markdown
  # Title 
  {lang=en}
  ```

  will output in `page.html`:

  ```html
  <!DOCTYPE html>

  <html lang="en">
  <head>
  </head>
        <h1>Title</h1>
  ```

* `headerHtmlFile`: Location of header HTML file as String,
  `${project.basedir}/src/main/resources/markdown/html/header.html`

  Example:

  ```html
  <!DOCTYPE html>
  <html lang="en">
  <head>
      <title>titleToken</title>
      <meta charset="utf-8" />
      <link rel="stylesheet" href="##SITE_BASE##/css/default.css">
  </head>
  ```

  Note: **`##SITE_BASE##`** will be translated to a relative path from the markdown file's
  directory to base directory. This is not necessary if *`recursiveInput`* configuration is
  `false`.

* `footerHtmlFile` : Location of header HTML file as String,
  `${project.basedir}/src/main/resources/markdown/html/footer.html`

  Example:

  ```html
    <footer>
      </footer>
  </html>
  ```

* `copyDirectories`: Comma separated list of directories to copy to output directory, like:
  `css,js,images,folder*/images,**images'

* `defaultTitle`: If set the titleToken is replaced in every page. Otherwise the first h1 is
  used.

* `recursiveInput`: Process also inputDirectory's sub directories if option `true`. Default
  `false`.

* `transformRelativeMarkdownLinks`: Transform relative url suffix from `.md` to `.html` if
  option `true`. Default `false`.

* `attributes`: defines a list of attributes by `Node` class to apply to HTML results. Each
  `attribute` has the syntax:
  `NodeClass|attributeName1=attributeValue1|attributeName2=attributeValue2` will add
  `attributeName1` and `attributeName2` to element node `NodeClass`

  for table, block quote and ordered list item class customization:

  ```xml
  <attributes>
      <attribute>TableBlock|class=table table-striped table-bordered</attribute>
      <attribute>BlockQuote|class=red</attribute>
      <attribute>OrderedListItem|style="color:red;"</attribute>
  </attributes>
  ```

  Core Nodes:

  * AutoLink

  * BlockQuote

  * BulletList

  * BulletListItem

  * Code

  * Emphasis

  * FencedCodeBlock

  * Heading

  * Image

  * ImageRef

  * IndentedCodeBlock

  * Link

  * LinkRef

  * MailLink

  * OrderedList

  * OrderedList

  * OrderedListItem

  * Strikethrough

  * StrongEmphasis

  * TableBlock

  * TableBody

  * TableCaption

  * TableCell

  * TableHead

  * TableRow

  * ThematicBreak

* `pegdownExtensions`: Comma separated list of constants as specified in
  `com.vladsch.flexmark.profiles.pegdown.Extensions`. The default is `TABLES`.

  :information_source: [flexmark-java] has many more extensions and configuration options than
  [pegdown] in addition to extensions available in pegdown 1.6.0, the following extensions are
  available:

  * `SMARTS`: Beautifies `...` `. . .`, `--` and `---` to `…`, `…`, `–` and `—` respectively.

  * `QUOTES`: Beautifies single quotes `'`, `"`, `<<` and `>>` to `‘` `’` `‛`, `“` `”` `‟`, `«`
    and `»`

  * `SMARTYPANTS`: Convenience extension enabling both, `SMARTS` and `QUOTES`, at once.

  * `ABBREVIATIONS`: Abbreviations in the way of [PHP Markdown Extra].

  * `ANCHORLINKS`: Generate anchor links for headers by taking the first range of alphanumerics
    and spaces.

  * `HARDWRAPS`: Alternative handling of newlines, see [Github-flavoured-Markdown]

  * `AUTOLINKS`: Plain, undelimited autolinks the way [Github-flavoured-Markdown] implements
    them.

  * `TABLES`: Tables similar to [MultiMarkdown] (which is in turn like the
    [PHP Markdown Extra: tables] tables, but with colspan support).

  * `DEFINITIONS`: Definition lists in the way of [PHP Markdown Extra: definition list].

  * `FENCED_CODE_BLOCKS`: Fenced Code Blocks in the way of [PHP Markdown Extra: fenced code] or
    [Github-flavoured-Markdown].

  * `SUPPRESS_HTML_BLOCKS`: Suppresses the output of HTML blocks.

  * `SUPPRESS_INLINE_HTML`: Suppresses the output of inline HTML elements.

  * `WIKILINKS`: Support `[[Wiki-style links]]` with a customizable URL rendering logic.

  * `STRIKETHROUGH`: Support `~~strikethroughs~~` as supported in [Pandoc] and
    [Github-flavoured-Markdown].

  * `ATXHEADERSPACE`: Require a space between the `#` and the header title text, as per
    [Github-flavoured-Markdown]. Frees up `#` without a space to be just plain text.

  * `FORCELISTITEMPARA`: Wrap a list item or definition term in `<p>` tags if it contains more
    than a simple paragraph.

  * `RELAXEDHRULES`: allow horizontal rules without a blank line following them.

  * `TASKLISTITEMS`: parses bullet lists of the form `* [ ]`, `* [x]` and `* [X]` to create
    [Github-flavoured-Markdown] task list items.

  * `EXTANCHORLINKS`: Generate anchor links for headers using complete contents of the header.
    * Spaces and non-alphanumerics replaced by `-`, multiple dashes trimmed to one.
    * Anchor link is added as first element inside the header with empty content: `<h1><a
      name="header"></a>header</h1>`

  * `EXTANCHORLINKS_WRAP`: used in conjunction with above to create an anchor that wraps header
    content: `<h1><a name="header">header</a></h1>`

  * `TOC`: used to enable table of contents extension `[TOC]` The TOC tag has the following
    format: `[TOC style]`. `style` consists of space separated list of options:
    * `levels=levelList` where level list is a comma separated list of levels or ranges. Default
      is to include heading levels 2 and 3. Examples:
      * `levels=4` include levels 2, 3 and 4
      * `levels=2-4` include levels 2, 3 and 4. same as `levels=4`
      * `levels=2-4,5` include levels 2, 3, 4 and 5
      * `levels=1,3` include levels 1 and 3
    * `text` to only include the text of the heading
    * `formatted` to include text and inline formatting
    * `bullet` to use a bullet list for the TOC items
    * `numbered` to use a numbered list for TOC items
    * `hierarchy`: hierarchical list of headings
    * `flat`: flat list of headings
    * `reversed`: flat reversed list of headings
    * `increasing`: flat, alphabetically increasing by heading text
    * `decreasing`: flat, alphabetically decreasing by heading text

  * `MULTI_LINE_IMAGE_URLS`: enables parsing of image urls spanning more than one line the
    format is strict `![alt text](urladdress?` must be the last non-blank segment on a line. The
    terminating `)` or `"title")` must be the first non-indented segment on the line. Everything
    in between is sucked up as part of the URL except for blank lines.

  * `RELAXED_STRONG_EMPHASIS_RULES`: allow Strong/Emphasis marks to start when not preceded by
    alphanumeric for `_` and as long as not surrounded by spaces for `*` instead of only when
    preceded by spaces.

  * `FOOTNOTES`: Support MultiMarkdown style footnotes: `[^n] for footnote reference` and `[^n]:
    Footnote text` for footnotes. Where `n` is one or more digit, letter, `-`, `_` or `.`.
    Footnotes will be put at the bottom of the page, sequentially numbered in order of
    appearance of the footnote reference. Footnotes that are not referenced will NOT be included
    in the HTML output.

    ```markdown
    This paragraph has a footnote[^1] and another footnote[^two].

    This one has more but out of sequence[^4] and[^eight].

    [^two]: Footnote 2 with a bit more text
        and another continuation line

    [^1]: Footnote 1

    [^3]: Unused footnote, it will not be added to the end of the page.

    [^4]: Out of sequence footnote

    [^eight]: Have one that is used.
    ```

    will generate:

    ```html
    <p>This paragraph has a footnote<sup id="fnref-1"><a href="#fn-1" class="footnote-ref">1</a></sup> and another footnote<sup id="fnref-2"><a href="#fn-2" class="footnote-ref">2</a></sup>.</p>
    <p>This one has more but out of sequence<sup id="fnref-3"><a href="#fn-3" class="footnote-ref">3</a></sup> and<sup id="fnref-4"><a href="#fn-4" class="footnote-ref">4</a></sup>. </p>
    <div class="footnotes">
    <hr/>
    <ol>
    <li id="fn-1"><p>Footnote 1<a href="#fnref-1" class="footnote-backref">&#8617;</a></p></li>
    <li id="fn-2"><p>Footnote 2 with a bit more text  and another continuation line<a href="#fnref-2" class="footnote-backref">&#8617;</a></p></li>
    <li id="fn-3"><p>Out of sequence footnote<a href="#fnref-3" class="footnote-backref">&#8617;</a></p></li>
    <li id="fn-4"><p>Have one that is used.<a href="#fnref-4" class="footnote-backref">&#8617;</a></p></li>
    </ol>
    </div>
    ```

    to look like this:

    <div>
        <hr/>
        <p>This paragraph has a footnote<sup id="fnref-1"><a href="#fn-1" class="footnote-ref">1</a></sup> and another footnote<sup id="fnref-2"><a href="#fn-2" class="footnote-ref">2</a></sup>.</p>
        <p>This one has more but out of sequence<sup id="fnref-3"><a href="#fn-3" class="footnote-ref">3</a></sup> and<sup id="fnref-4"><a href="#fn-4" class="footnote-ref">4</a></sup>. </p>
        <hr/>
        <div class="footnotes">
           <ol style="list-style-type: decimal;">
               <li id="fn-1"><p>Footnote 1<a href="#fnref-1" class="footnote-backref">&#8617;</a></p></li>
               <li id="fn-2"><p>Footnote 2 with a bit more text  and another continuation line<a href="#fnref-2" class="footnote-backref">&#8617;</a></p></li>
               <li id="fn-3"><p>Out of sequence footnote<a href="#fnref-3" class="footnote-backref">&#8617;</a></p></li>
               <li id="fn-4"><p>Have one that is used.<a href="#fnref-4" class="footnote-backref">&#8617;</a></p></li>
           </ol>
        </div>
    </div>

  * `SUBSCRIPT`: subscript extension `~subscript~`

  * `SUPERSCRIPT`: superscript extension `^superscript^`

  * `INSERTED`: inserted or underlined extension `++inserted++`

* `inputFileExtensions`: Comma-separated input file extensions (auto trim included), default:
  `md`

Example

```xml
  <configuration>
    <inputFileExtensions> md, markdown </inputFileExtensions>
  </configuration>
```

* `inputEncoding`: Charset-Name used for reading the md-input, default:
  `${project.build.sourceEncoding}` or `Default-Charset`

* outputEncoding: Charset-Name used for writing the html-output, default:
  `${project.build.sourceEncoding}` or `Default-Charset`

The output will be:

* `target/html/name_of_file.html`

## Configuration

Add the plugin to the pom file in your project:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>com.ruleoftech</groupId>
            <artifactId>markdown-page-generator-plugin</artifactId>
            <version>2.1.0</version>
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
```

Or with custom header and footer:

```xml
<plugin>
    <groupId>com.ruleoftech</groupId>
    <artifactId>markdown-page-generator-plugin</artifactId>
    <version>2.1.0</version>
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
```

You can also specify the Pegdown extensions:

```xml
<plugin>
    <groupId>com.ruleoftech</groupId>
    <artifactId>markdown-page-generator-plugin</artifactId>
    <version>2.1.0</version>
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
```

Input- and Output-Encoding can be specified by:

```xml
<plugin>
    <groupId>com.ruleoftech</groupId>
    <artifactId>markdown-page-generator-plugin</artifactId>
    <version>2.1.0</version>
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
```

[flexmark-java]: https://github.com/vsch/flexmark-java
[Github-flavoured-Markdown]: http://github.github.com/github-flavored-markdown/
[MultiMarkdown]: http://fletcherpenney.net/multimarkdown/
[Pandoc]: http://pandoc.org/MANUAL.html#pandocs-markdown
[pegdown]: http://pegdown.org
[PHP Markdown Extra]: http://michelf.com/projects/php-markdown/extra/#abbr
[PHP Markdown Extra: definition list]: http://michelf.com/projects/php-markdown/extra/#def-list
[PHP Markdown Extra: fenced code]: http://michelf.com/projects/php-markdown/extra/#fenced-code-blocks
[PHP Markdown Extra: tables]: http://michelf.com/projects/php-markdown/extra/#table
