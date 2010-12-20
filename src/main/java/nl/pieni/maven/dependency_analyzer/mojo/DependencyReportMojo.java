/*
 * Copyright  2010 Pieter van der Meer (pieter@pieni.nl)
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

package nl.pieni.maven.dependency_analyzer.mojo;

import nl.pieni.maven.dependency_analyzer.database.DependencyDatabase;
import nl.pieni.maven.dependency_analyzer.database.DependencyDatabaseSearcher;
import nl.pieni.maven.dependency_analyzer.neo4j.database.DependencyDatabaseImpl;
import nl.pieni.maven.dependency_analyzer.neo4j.database.DependencyDatabaseSearcherImpl;
import nl.pieni.maven.dependency_analyzer.node.ArtifactNode;
import nl.pieni.maven.dependency_analyzer.report.DependencyReport;
import nl.pieni.maven.dependency_analyzer.report.impl.DependencyReportImpl;
import nl.pieni.maven.dependency_analyzer.report.log.LogWriter;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.IOException;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Depenency grahpDB reporting Mojo.
 *
 * @goal report
 * @phase process-sources
 * @requiredProject false
 */
public class DependencyReportMojo extends AbstractDependencyMojo {

    /**
     * The type of artifacts to search. remember, the search is performed against the packaging that is defined in the
     * pom file used to create the artifact. Format: "groupId:artifactId"
     *
     * @parameter property="reportArtifacts"
     */
    private List<String> reportArtifacts;


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        DependencyDatabase database = new DependencyDatabaseImpl(getLog(), databaseDirectory);
        DependencyDatabaseSearcher searcher = new DependencyDatabaseSearcherImpl(getLog(), database);
        DependencyReport reporter = new DependencyReportImpl(searcher);
        LogWriter logWriter = new LogWriter(getLog());

        for (String reportArtifact : reportArtifacts) {
            getLog().info("\nReport for artifact: " + reportArtifact);
            Dependency dependency = stringToGaDependency(reportArtifact);
            if (dependency == null){
                return;
            }

            //Find the Node
            ArtifactNode artifactNode = searcher.findArtifactNode(dependency);
            if (artifactNode == null) {
                continue;
            }

            try {
                reporter.createReport(dependency, logWriter);
            } catch (IOException e) {
                throw  new MojoExecutionException("Error creating output for reporting", e);
            }
        }
    }

    /**
     * Convert the string (groupId:artifactId) to a {@link Dependency} object.
     * @param reportArtifact The G:A string
     * @return a dependency object
     */
    private Dependency stringToGaDependency(String reportArtifact) {
        StringTokenizer strTok = new StringTokenizer(reportArtifact, ":");
        if (strTok.countTokens() != 2) {
            getLog().error("reportArtifacts string: " + reportArtifact + " is not valid, requires groupId:artifactId");
            return null;
        }
        String groupId = strTok.nextToken();
        String artifactId = strTok.nextToken();
        Dependency dependency = new Dependency();
        dependency.setGroupId(groupId);
        dependency.setArtifactId(artifactId);
        return dependency;
    }
}
