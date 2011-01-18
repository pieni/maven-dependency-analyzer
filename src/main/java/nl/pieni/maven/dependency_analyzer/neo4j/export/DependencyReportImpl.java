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
package nl.pieni.maven.dependency_analyzer.neo4j.export;

import nl.pieni.maven.dependency_analyzer.database.DependencyDatabaseSearcher;
import nl.pieni.maven.dependency_analyzer.neo4j.enums.DependencyScopeRelations;
import nl.pieni.maven.dependency_analyzer.node.ArtifactNode;
import nl.pieni.maven.dependency_analyzer.node.GroupNode;
import nl.pieni.maven.dependency_analyzer.node.VersionNode;
import nl.pieni.maven.dependency_analyzer.export.DependencyReport;
import org.apache.maven.model.Dependency;
import org.neo4j.graphdb.Node;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

/**
 * Place holder for the data collected for the report.
 */
public class DependencyReportImpl implements DependencyReport {
    private final String lineSeperator = System.getProperty("line.separator");
    private final DependencyDatabaseSearcher<Node> searcher;

    /**
     * Default constructor
     *
     * @param dependencyDatabaseSearcher the Searcher
     */
    @SuppressWarnings("unchecked")
    public DependencyReportImpl(DependencyDatabaseSearcher dependencyDatabaseSearcher) {
        this.searcher = dependencyDatabaseSearcher;
    }

    /**
     * @inheritDoc
     */
    @Override
    public void createReport(Dependency dependency, Writer writer) throws IOException {

        ArtifactNode artifactNode = searcher.findArtifactNode(dependency);
        if (artifactNode == null) {
            return;
        }

        List<VersionNode> versions = searcher.getVersionNodes(dependency);
        GroupNode groupNode = artifactNode.getParent();
        writer.write("Report for Artifact: \"" + groupNode.getGroupId() + ":" + artifactNode.getArtifactId() + "\"" + lineSeperator);
        writer.write("Available versions" + lineSeperator);
        for (VersionNode version : versions) {
            writer.write("\t" + version.getVersion() + lineSeperator);
        }

        Map<DependencyScopeRelations, List<ArtifactNode>> scopedRelations = searcher.getDependingArtifacts(dependency);
        writer.write("Incoming relations" + lineSeperator);
        for (DependencyScopeRelations dependencyScope : scopedRelations.keySet()) {
            writer.write("\tScope: " + dependencyScope + lineSeperator);

            List<ArtifactNode> artifacts = scopedRelations.get(dependencyScope);
            for (ArtifactNode artifact : artifacts) {
                GroupNode relatedGroupNode = artifact.getParent();
                writer.write("\t\t" + relatedGroupNode.getGroupId() + ":" + artifact.getArtifactId() + ":" + artifact.getType() + lineSeperator);
            }
        }

        Map<VersionNode, List<VersionNode>> versionDependencies = searcher.getVersionDependencies(dependency);
        writer.write("Version specific relations" + lineSeperator);
        for (VersionNode parentVersion : versionDependencies.keySet()) {
            writer.write("\t" + parentVersion.getVersion() + lineSeperator);
            List<VersionNode> artifacts = versionDependencies.get(parentVersion);
            for (VersionNode version : artifacts) {
                ArtifactNode versionArtifact = version.getParent();
                GroupNode versionGroup = versionArtifact.getParent();

                writer.write("\t\t" + versionGroup.getGroupId() + ":" + versionArtifact.getArtifactId() + ":" + versionArtifact.getType() + ":" + version.getVersion() + lineSeperator);
            }
        }
    }
}
