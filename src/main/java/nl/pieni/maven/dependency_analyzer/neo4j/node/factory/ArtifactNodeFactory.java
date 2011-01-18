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

package nl.pieni.maven.dependency_analyzer.neo4j.node.factory;

import nl.pieni.maven.dependency_analyzer.database.DependencyDatabase;
import nl.pieni.maven.dependency_analyzer.database.DependencyDatabaseSearcher;
import nl.pieni.maven.dependency_analyzer.neo4j.enums.ArtifactRelations;
import nl.pieni.maven.dependency_analyzer.neo4j.node.ArtifactNodeDecorator;
import nl.pieni.maven.dependency_analyzer.neo4j.node.GroupNodeDecorator;
import nl.pieni.maven.dependency_analyzer.node.ArtifactNode;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.logging.Log;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import static nl.pieni.maven.dependency_analyzer.neo4j.enums.NodeProperties.ARTIFACT_ID;


/**
 * An artifact Node
 */
public class ArtifactNodeFactory extends AbstractNodeFactory<ArtifactNode> {

    /**
     * {@inheritDoc}
     */
    public ArtifactNodeFactory(DependencyDatabase<GraphDatabaseService, Node> database, DependencyDatabaseSearcher<Node> searcher, final Log logger) {
        super(database, searcher, logger);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ArtifactNode create(final Dependency dependency) {
        Node node = getDatabase().createNode();
        ArtifactNode artifactNode = new ArtifactNodeDecorator(node, dependency);
        getSearcher().indexOnProperty(node, ARTIFACT_ID);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Created ArtifactNode: " + artifactNode);
        }
        return artifactNode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int insert(final Dependency dependency) {
        int nodeCount = 0;
        GroupNodeDecorator groupNode = (GroupNodeDecorator) getSearcher().findGroupNode(dependency);
        ArtifactNodeDecorator artifactNode = (ArtifactNodeDecorator) getSearcher().findArtifactNode(dependency);
        if (artifactNode == null) {
            getDatabase().startTransaction();
            artifactNode = (ArtifactNodeDecorator) create(dependency);
            nodeCount++;
            Relationship relationship = groupNode.createRelationshipTo(artifactNode, ArtifactRelations.has);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Added " + relationship + " between " + groupNode + " and " + artifactNode);
            }
            getDatabase().stopTransaction();
        }
        return nodeCount;
    }
}
