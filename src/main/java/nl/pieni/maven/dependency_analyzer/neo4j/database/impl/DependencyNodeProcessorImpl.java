/*
 * Copyright 2010 Pieter van der Meer (pieter(at)pieni.nl)
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

package nl.pieni.maven.dependency_analyzer.neo4j.database.impl;

import nl.pieni.maven.dependency_analyzer.neo4j.database.DependencyDatabase;
import nl.pieni.maven.dependency_analyzer.neo4j.database.DependencyNodeProcessor;
import nl.pieni.maven.dependency_analyzer.neo4j.enums.ArtifactRelations;
import nl.pieni.maven.dependency_analyzer.neo4j.enums.DependencyScopeRelations;
import nl.pieni.maven.dependency_analyzer.neo4j.node.ArtifactNodeFactory;
import nl.pieni.maven.dependency_analyzer.neo4j.node.GroupNodeFactory;
import nl.pieni.maven.dependency_analyzer.neo4j.node.VersionNodeFactory;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.logging.Log;
import org.jetbrains.annotations.NotNull;
import org.neo4j.graphdb.*;

/**
 * Processing of Dependency elements
 */
public class DependencyNodeProcessorImpl implements DependencyNodeProcessor {
    private ArtifactNodeFactory artifactNodeFactory;
    private GroupNodeFactory groupNodeFactory;
    private VersionNodeFactory versionNodeFactory;
    private final DependencyDatabase database;
    private final Log logger;

    public DependencyNodeProcessorImpl(DependencyDatabase database, final Log logger) {
        this.database = database;
        this.logger = logger;
        artifactNodeFactory = new ArtifactNodeFactory(database, logger);
        groupNodeFactory = new GroupNodeFactory(database, logger);
        versionNodeFactory = new VersionNodeFactory(database, logger);
    }

    @Override
    public int addArtifact(@NotNull final Dependency dependency) {
        int nodeCount = 0;
        if (getLog().isDebugEnabled()) {
            getLog().debug("Adding nodes for artifact: " + dependency);
        }

        nodeCount += groupNodeFactory.insert(dependency);

        nodeCount += artifactNodeFactory.insert(dependency);

        nodeCount += versionNodeFactory.insert(dependency);

        return nodeCount;
    }

    @Override
    public int addRelation(@NotNull final Dependency sourceDependency, @NotNull final Dependency targetDependency) {
        int count = 0;
        Node sourceArtifactNode = database.getSearcher().findArtifactNode(sourceDependency);
        Node targetArtifactNode = database.getSearcher().findArtifactNode(targetDependency);


        RelationshipType relationType = determineRelationType(targetDependency);


        if (!hasDependencyRelation(sourceArtifactNode, targetArtifactNode, relationType)) {
            database.startTransaction();
            sourceArtifactNode.createRelationshipTo(targetArtifactNode, relationType);
            count++;
            getLog().info("Added " + relationType + " between " + sourceDependency + " and " + targetDependency);
            database.stopTransaction();
        }

        Node targetVersionNode = database.getSearcher().findVersionNode(targetDependency);
        Node sourceVersionNode = database.getSearcher().findVersionNode(sourceDependency);
        if (!hasDependencyRelation(sourceVersionNode, targetVersionNode, ArtifactRelations.VersionsDependency)) {
            database.startTransaction();
            sourceVersionNode.createRelationshipTo(targetVersionNode, ArtifactRelations.VersionsDependency);
            count++;
            getLog().info("Added " + ArtifactRelations.VersionsDependency + " between " + sourceDependency + " and " + targetDependency);
            database.stopTransaction();
        }
        return count;
    }

    private boolean hasDependencyRelation(Node sourceArtifactNode, Node targetArtifactNode, RelationshipType type) {
        Iterable<Relationship> relations = sourceArtifactNode.getRelationships(type, Direction.OUTGOING);

        for (Relationship relation : relations) {
            Node node = relation.getOtherNode(sourceArtifactNode);
            if (node.getId() == targetArtifactNode.getId()) {
                return true;
            }
        }
        return false;  //To change body of created methods use File | Settings | File Templates.
    }

    private RelationshipType determineRelationType(@NotNull final Dependency dependency) {
        return DependencyScopeRelations.fromString(dependency.getScope());
    }

    public Log getLog() {
        return logger;
    }
}
