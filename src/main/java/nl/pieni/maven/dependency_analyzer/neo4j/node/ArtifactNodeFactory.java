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

package nl.pieni.maven.dependency_analyzer.neo4j.node;

import nl.pieni.maven.dependency_analyzer.neo4j.database.DependencyDatabase;
import nl.pieni.maven.dependency_analyzer.neo4j.enums.ArtifactRelations;
import nl.pieni.maven.dependency_analyzer.neo4j.enums.NodeType;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.logging.Log;
import org.jetbrains.annotations.NotNull;
import org.neo4j.graphdb.Node;

import static nl.pieni.maven.dependency_analyzer.neo4j.enums.NodeProperties.ARTIFACT_ID;
import static nl.pieni.maven.dependency_analyzer.neo4j.enums.NodeProperties.TYPE;


/**
 * An artifact Node
 */
public class ArtifactNodeFactory extends AbstractNodeFactory {

    /**
     * {@inheritDoc}
     */
    public ArtifactNodeFactory(DependencyDatabase database, final Log logger) {
        super(database, logger);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Node create(@NotNull final Dependency dependency) {
        getDatabase().startTransaction();
        Node node = createNode(NodeType.ArtifactNode);
        node.setProperty(ARTIFACT_ID, dependency.getArtifactId());
        indexOnProperty(node, ARTIFACT_ID);
        node.setProperty(TYPE, dependency.getType());
        LOGGER.info("Create ArtifactNode: " + node2String(node));
        getDatabase().stopTransaction();
        return node;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int insert(@NotNull final Dependency dependency) {
        int nodeCount = 0;
        Node groupNode = getDatabase().getSearcher().findGroupNode(dependency);
        Node artifactNode = getDatabase().getSearcher().findArtifactNode(dependency);
        if (artifactNode == null) {
            artifactNode = create(dependency);
            nodeCount++;
            getDatabase().startTransaction();
            groupNode.createRelationshipTo(artifactNode, ArtifactRelations.has);
            LOGGER.info("Created relation " + ArtifactRelations.has + "between " + node2String(groupNode) + " and " + node2String(artifactNode));
            getDatabase().stopTransaction();
        }
        return nodeCount;
    }
}
