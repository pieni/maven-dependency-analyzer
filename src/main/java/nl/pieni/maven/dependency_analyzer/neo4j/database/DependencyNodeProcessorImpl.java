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

package nl.pieni.maven.dependency_analyzer.neo4j.database;

import nl.pieni.maven.dependency_analyzer.database.DependencyDatabase;
import nl.pieni.maven.dependency_analyzer.database.DependencyDatabaseSearcher;
import nl.pieni.maven.dependency_analyzer.database.DependencyNodeProcessor;
import nl.pieni.maven.dependency_analyzer.neo4j.enums.ArtifactRelations;
import nl.pieni.maven.dependency_analyzer.neo4j.enums.ScopedRelation;
import nl.pieni.maven.dependency_analyzer.neo4j.node.ArtifactNodeDecorator;
import nl.pieni.maven.dependency_analyzer.neo4j.node.VersionNodeDecorator;
import nl.pieni.maven.dependency_analyzer.neo4j.node.factory.ArtifactNodeFactory;
import nl.pieni.maven.dependency_analyzer.neo4j.node.factory.GroupNodeFactory;
import nl.pieni.maven.dependency_analyzer.neo4j.node.factory.VersionNodeFactory;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.logging.Log;
import org.neo4j.graphdb.*;

/**
 * Processing of Dependency elements
 */
public class DependencyNodeProcessorImpl implements DependencyNodeProcessor {
    private final ArtifactNodeFactory artifactNodeFactory;
    private final GroupNodeFactory groupNodeFactory;
    private final VersionNodeFactory versionNodeFactory;
    private final DependencyDatabase<GraphDatabaseService, Node> database;
    private final DependencyDatabaseSearcher searcher;
    private final Log logger;

    /**
     * Default constructor
     *
     * @param database the database instance
     * @param searcher The searcher
     * @param logger   the Logger
     */
    @SuppressWarnings("unchecked")
    public DependencyNodeProcessorImpl(DependencyDatabase database, DependencyDatabaseSearcher searcher, final Log logger) {
        this.database = database;
        this.searcher = searcher;
        this.logger = logger;
        artifactNodeFactory = new ArtifactNodeFactory(database, searcher, logger);
        groupNodeFactory = new GroupNodeFactory(database, searcher, logger);
        versionNodeFactory = new VersionNodeFactory(database, searcher, logger);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int addArtifact(final Dependency dependency) {
        int nodeCount = 0;

        getLog().info("Adding nodes for artifact: " + dependency);

        nodeCount += groupNodeFactory.insert(dependency);

        nodeCount += artifactNodeFactory.insert(dependency);

        nodeCount += versionNodeFactory.insert(dependency);

        return nodeCount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int addRelation(final Dependency sourceDependency, final Dependency targetDependency) {
        int count = 0;
        ArtifactNodeDecorator sourceArtifactNode = (ArtifactNodeDecorator) searcher.findArtifactNode(sourceDependency);
        ArtifactNodeDecorator targetArtifactNode = (ArtifactNodeDecorator) searcher.findArtifactNode(targetDependency);


        RelationshipType relationType = determineRelationType(targetDependency);
        database.startTransaction();

        if (!hasDependencyRelation(sourceArtifactNode, targetArtifactNode, relationType)) {
            Relationship relationship = sourceArtifactNode.createRelationshipTo(targetArtifactNode, relationType);
            count++;
            if (getLog().isDebugEnabled()) {
                getLog().debug("Added " + relationship + " between " + sourceArtifactNode + " and " + targetArtifactNode);
            }
        }

        VersionNodeDecorator targetVersionNode = (VersionNodeDecorator) searcher.findVersionNode(targetDependency);
        VersionNodeDecorator sourceVersionNode = (VersionNodeDecorator) searcher.findVersionNode(sourceDependency);
        if (!hasDependencyRelation(sourceVersionNode, targetVersionNode, ArtifactRelations.depends)) {

            Relationship relationship = sourceVersionNode.createRelationshipTo(targetVersionNode, ArtifactRelations.depends);
            count++;
            if (getLog().isDebugEnabled()) {
                getLog().debug("Added " + relationship + " between " + sourceVersionNode + " and " + targetVersionNode);
            }
        }

        database.stopTransaction();

        return count;
    }

    /**
     * See of the source node and artifact node have a specific relation
     *
     * @param sourceArtifactNode the source
     * @param targetArtifactNode the target
     * @param type               the relation
     * @return true when the relation is present
     */
    private boolean hasDependencyRelation(Node sourceArtifactNode, Node targetArtifactNode, RelationshipType type) {
        Iterable<Relationship> relations = sourceArtifactNode.getRelationships(type, Direction.OUTGOING);

        for (Relationship relation : relations) {
            Node node = relation.getOtherNode(sourceArtifactNode);
            if (node.getId() == targetArtifactNode.getId()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Convert the {@link Dependency} scope attribute to a {@link RelationshipType}
     *
     * @param dependency the dependency
     * @return the {@link RelationshipType}
     */
    private RelationshipType determineRelationType(final Dependency dependency) {
        RelationshipType result = ScopedRelation.fromString(dependency.getScope());
        if (result == null) {
            throw new IllegalArgumentException("Unable to determine scope for dependency: " + dependency);
        }
        return result;
    }

    /**
     * Get the logger.
     *
     * @return the logger
     */
    private Log getLog() {
        return logger;
    }
}
