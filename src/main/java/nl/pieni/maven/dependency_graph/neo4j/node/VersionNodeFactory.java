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

package nl.pieni.maven.dependency_graph.neo4j.node;

import nl.pieni.maven.dependency_graph.neo4j.database.DependencyDatabase;
import nl.pieni.maven.dependency_graph.neo4j.enums.ArtifactRelations;
import nl.pieni.maven.dependency_graph.neo4j.enums.NodeType;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.logging.Log;
import org.jetbrains.annotations.NotNull;
import org.neo4j.graphdb.*;
import org.neo4j.index.IndexService;

import static nl.pieni.maven.dependency_graph.neo4j.enums.NodeProperties.VERSION;

/**
 * version Node
 */
public class VersionNodeFactory extends AbstractNodeFactory {


    /**
     * {@inheritDoc}
     */
    public VersionNodeFactory(DependencyDatabase database, final Log logger) {
        super(database, logger);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Node create(@NotNull final Dependency dependency) {
        getDatabase().startTransaction();
        Node node = createNode(NodeType.VersionNode);
        node.setProperty(VERSION, dependency.getVersion());
        LOGGER.info("Create versionNode: " + node2String(node));
        getDatabase().stopTransaction();
        return node;
    }

    /**
     * {@inheritDoc}
     */
    public int insert(@NotNull final Dependency dependency) {
        int nodeCount = 0;
        Node artifactNode = getDatabase().getSearcher().findArtifactNode(dependency);
        Iterable<Relationship> versionsIterable = artifactNode.getRelationships(ArtifactRelations.version, Direction.OUTGOING);
        Node versionNode = null;

        for (Relationship relationship : versionsIterable) {
            Node verNode = relationship.getOtherNode(artifactNode);
            if (dependency.getVersion().equals(verNode.getProperty(VERSION))) {
                versionNode = verNode;
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Found versionNode " + node2String(verNode));
                }
                break;
            }
        }

        if (versionNode == null) {
            versionNode = create(dependency);
            nodeCount++;
            getDatabase().startTransaction();
            artifactNode.createRelationshipTo(versionNode, ArtifactRelations.version);
            LOGGER.info("Created relation " + ArtifactRelations.version + "between " + node2String(artifactNode) + " and " + node2String(versionNode));
            getDatabase().stopTransaction();
        }
        return nodeCount;
    }
}
