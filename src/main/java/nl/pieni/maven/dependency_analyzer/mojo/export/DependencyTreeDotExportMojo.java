/*
 * Copyright (c) 2011 Pieter van der Meer (pieter@pieni.nl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.pieni.maven.dependency_analyzer.mojo.export;

import nl.pieni.maven.dependency_analyzer.dot.DotExporter;
import nl.pieni.maven.dependency_analyzer.dot.NodeWriter;
import nl.pieni.maven.dependency_analyzer.neo4j.export.DotExporterImpl;
import nl.pieni.maven.dependency_analyzer.neo4j.export.NodeWriterImpl;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

/**
 * Depenency grahpDB reporting Mojo.
 *
 * @goal export-dot
 * @phase process-sources
 * @requiredProject false
 */
public class DependencyTreeDotExportMojo extends AbstractReportMojo {

    /**
     * File where the output is written.
     *
     * @parameter property="dotFile" default-value="target/dependency-graph.dot"
     */
    private String dotFile;

    /**
     * File where the output is written.
     *
     * @parameter property="includeVersions" default-value="false"
     */
    private boolean includeVersions;

    /**
     * List of groupId:artifactId elements for inclusion in the graph generated.
     * follows syntax from maven-common-artifact-filters (AbstractStrictPatternArtifactFilter)
     *
     * @parameter property="includeFilterPatterns"
     */
    private List<String> includeFilterPatterns;

    private DotExporter exporter;

    @SuppressWarnings("unchecked")
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        super.setup();

        exporter = new DotExporterImpl(getDatabase(), includeVersions, getLog());

        try {
            FileOutputStream fos = new FileOutputStream(dotFile);
            Writer osWriter = new OutputStreamWriter(fos, "UTF-8");
            NodeWriter writer = new NodeWriterImpl(osWriter, getLog());
            exporter.export(includeFilterPatterns, writer);
        } catch (IOException e) {
            getLog().error("Error creating output for reporting");
            throw new MojoExecutionException("Error creating output for reporting", e);
        } finally {
            tearDown();
        }
    }
}
