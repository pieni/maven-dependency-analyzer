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
package nl.pieni.maven.dependency_analyzer.report;

import nl.pieni.maven.dependency_analyzer.neo4j.enums.DependencyScopeRelations;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Place holder for the data collected for the report.
 */
public class DependencyReportImpl implements DependencyReport {
    private final String reportingArtifact;
    private final String lineSeperator;
    private List<String> versions = new ArrayList<String>();
    private Map<DependencyScopeRelations, List<String>> scopedRelations = new HashMap<DependencyScopeRelations, List<String>>();
    private Map<String, List<String>> versionRelations = new HashMap<String, List<String>>();

    /**
     * Default constructor
     *
     * @param reportingArtifact
     */
    public DependencyReportImpl(String reportingArtifact) {
        this.reportingArtifact = reportingArtifact;
        this.lineSeperator = System.getProperty("line.separator");

    }

    /*
    *  @inheritDoc
    */
    @Override
    public void addVersion(String version) {
        versions.add(version);
    }

    /*
    *  @inheritDoc
    */
    @Override
    public void addScopedRelation(DependencyScopeRelations scope, String artifact) {
        List<String> scopeList = scopedRelations.get(scope);
        if (scopeList == null) {
            scopeList = new ArrayList<String>();
            scopedRelations.put(scope, scopeList);
        }
        scopeList.add(artifact);
    }

    @Override
    public void addVersionRelation(String version, String artifact) {
        List<String> versionList = versionRelations.get(version);
        if (versionList == null) {
            versionList = new ArrayList<String>();
            versionRelations.put(version, versionList);
        }
        versionList.add(artifact);
    }

    /*
    *  @inheritDoc
    */
    @Override
    public void createReport(Writer writer) throws IOException {
        writer.write("Report for Artifact: \"" + reportingArtifact + "\"" + lineSeperator);
        writer.write("Available versions" + lineSeperator);
        for (String version : versions) {
            writer.write("\t" + version + "\n" + lineSeperator);
        }

        writer.write("Incoming relations" + lineSeperator);
        for (DependencyScopeRelations dependencyScope : scopedRelations.keySet()) {
            writer.write("\tScope: " + dependencyScope + lineSeperator);
            List<String> artifacts = scopedRelations.get(dependencyScope);
            for (String artifact : artifacts) {
                writer.write("\t\t" + artifact + lineSeperator);
            }
        }

        writer.write("Version specific relations" + lineSeperator);
        for (String version : versionRelations.keySet()) {
            writer.write("\t" + version + lineSeperator);
            List<String> artifacts = versionRelations.get(version);
            for (String artifact : artifacts) {
                writer.write("\t\t" + artifact + lineSeperator);
            }
        }
    }

    /*
     *  @inheritDoc
     */
    @Override
    public String getArtifact() {
        return this.reportingArtifact;
    }
}
