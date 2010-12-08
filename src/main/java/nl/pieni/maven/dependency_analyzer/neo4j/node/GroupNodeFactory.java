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

import static nl.pieni.maven.dependency_analyzer.neo4j.enums.NodeProperties.GROUP_ID;

/**
 * GroupNodeFactory
 */
public class GroupNodeFactory extends AbstractNodeFactory {


    /**
     * {@inheritDoc}
     */
    public GroupNodeFactory(DependencyDatabase database, final Log logger) {
        super(database, logger);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Node create(@NotNull final Dependency dependency) {
        getDatabase().startTransaction();
        Node node = createNode(NodeType.GroupNode);
        node.setProperty(GROUP_ID, dependency.getGroupId());
        indexOnProperty(node, GROUP_ID);
        LOGGER.info("Create GroupNode: " + node2String(node));
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
        if (groupNode == null) {
            groupNode = create(dependency);
            nodeCount++;
            getDatabase().startTransaction();
            getDatabase().getGraphDb().getReferenceNode().createRelationshipTo(groupNode, ArtifactRelations.has);

            LOGGER.info("Created relation " + ArtifactRelations.has + "between referenceNode and " + node2String(groupNode));

            getDatabase().stopTransaction();
        }
        return nodeCount;
    }

}
