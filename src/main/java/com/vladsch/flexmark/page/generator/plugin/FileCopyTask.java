/*
 * The MIT License
 *
 * Copyright 2017 raymoon.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.vladsch.flexmark.page.generator.plugin;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

/**
 *
 * @author raymoon
 */
public class FileCopyTask {

    private final Log log;

    public FileCopyTask(Log log) {
        this.log = log;
    }

    public Log getLog() {
        return log;
    }

    public void copyFiles(String inputDirectory, String outputDirectory, String selectedDirectory) throws MojoExecutionException {
        copyFiles(inputDirectory + File.separator + selectedDirectory, outputDirectory + File.separator + selectedDirectory);
    }

    public void copyFiles(String fromDir, String toDir) throws MojoExecutionException {
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
}
