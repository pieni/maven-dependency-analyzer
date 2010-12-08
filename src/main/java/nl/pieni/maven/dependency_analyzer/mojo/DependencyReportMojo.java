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

import nl.pieni.maven.dependency_analyzer.neo4j.database.DependencyDatabase;
import nl.pieni.maven.dependency_analyzer.neo4j.database.impl.DependencyDatabaseImpl;
import nl.pieni.maven.dependency_analyzer.neo4j.enums.ArtifactRelations;
import nl.pieni.maven.dependency_analyzer.neo4j.enums.DependencyScopeRelations;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.jetbrains.annotations.NotNull;
import org.neo4j.graphdb.*;

import java.util.List;
import java.util.StringTokenizer;

import static nl.pieni.maven.dependency_analyzer.neo4j.enums.NodeProperties.*;

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
     * pom file used to create the artifact.
     *
     * @parameter property="reportArtifacts"
     */
    private List<String> reportArtifacts;

    DependencyDatabase dependencyDatabase;


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        dependencyDatabase = new DependencyDatabaseImpl(getLog(), databaseDirectory);
        for (String reportArtifact : reportArtifacts) {
            getLog().info("Report for artifact: " + reportArtifact);
            StringTokenizer strTok = new StringTokenizer(reportArtifact, ":");
            String groupId = strTok.nextToken();
            String artifactId = strTok.nextToken();
            Dependency dependency = new Dependency();
            dependency.setGroupId(groupId);
            dependency.setArtifactId(artifactId);

            Node artifactNode = dependencyDatabase.getSearcher().findArtifactNode(dependency);

            reportVersions(artifactNode);
            reportIncomingRelations(artifactNode);
            reportVersionDependencies(artifactNode);

        }
    }

    private void reportVersionDependencies(Node node) {
        getLog().info("Version specific relations: ");
        Iterable<Relationship> availableVersionRelations = node.getRelationships(ArtifactRelations.version, Direction.OUTGOING);

        for (Relationship relationship : availableVersionRelations) {
            Node versionNode = relationship.getOtherNode(node);
            getLog().info("\tVersion: " + getProperty(versionNode, VERSION));
            Iterable<Relationship> versionRelations = versionNode.getRelationships(ArtifactRelations.VersionsDependency, Direction.INCOMING);
            for (Relationship versionRelation : versionRelations) {
                Node relationNode = versionRelation.getOtherNode(versionNode);
                getLog().info("\t\t" + versionNodeComplete(relationNode));
            }
        }
    }

    /**
     * Print Relations to the specified node. Only the relations defined in {@link nl.pieni.maven.dependency_analyzer.neo4j.enums.DependencyScopeRelations}
     * are processed
     *
     * @param node the parent for the report.
     */
    private void reportIncomingRelations(Node node) {
        getLog().info("Incoming relations artifacts: ");
        DependencyScopeRelations[] relations = DependencyScopeRelations.values();
        for (DependencyScopeRelations relation : relations) {
            getLog().info("\tScope: " + relation);
            Iterable<Relationship> scopeRelations = node.getRelationships(relation, Direction.INCOMING);
            for (Relationship scopeRelation : scopeRelations) {
                Node scopeNode = scopeRelation.getOtherNode(node);
                String id = getArtifactInformation(scopeNode);
                getLog().info("\t\t" + id);
            }
        }
    }

    /**
     * Retrieve a property from the node.
     *
     * @param node the node
     * @param key  the key
     * @return it value ("NotFound" if not present)
     */
    private String getProperty(@NotNull Node node, String key) {
        try {
            return node.getProperty(key).toString();
        } catch (NotFoundException e) {
            return "NotFound";
        }
    }

    /**
     * Print all available versions of the Node
     *
     * @param node the node
     */
    private void reportVersions(Node node) {
        getLog().info("Available versions: ");
        Iterable<Relationship> versions = node.getRelationships(ArtifactRelations.version, Direction.OUTGOING);
        for (Relationship relationship : versions) {
            Node versionNode = relationship.getOtherNode(node);
            getLog().info("\t" + getProperty(versionNode, VERSION));
        }
    }



    /**
     * Create a string that holds the groupId:ArtifactId:type
     *
     * @param artifactNode the node
     * @return groupId:ArtifactId:type
     */
    private String getArtifactInformation(Node artifactNode) {
        Iterable<Relationship> hasRelations = artifactNode.getRelationships(ArtifactRelations.has, Direction.INCOMING);

        for (Relationship relationship : hasRelations) {
            Node relationNode = relationship.getOtherNode(artifactNode);
            return getProperty(relationNode, GROUP_ID) + ":" + getProperty(artifactNode, ARTIFACT_ID) + ":" + getProperty(artifactNode, TYPE);
        }

        return "unable to determine";
    }

    /**
     * * Create a string that holds the groupId:ArtifactId:type
     * @param node a versionNode
     * @return groupId:ArtifactId:type:version
     */
    private String versionNodeComplete(Node node) {
        Iterable<Relationship> parentRelations = node.getRelationships(ArtifactRelations.version, Direction.INCOMING);
        for (Relationship parentRelation : parentRelations) {
            Node parent = parentRelation.getOtherNode(node);
            return getArtifactInformation(parent) + ":" + getProperty(node, VERSION);
        }

        return "unknown";
    }


}
