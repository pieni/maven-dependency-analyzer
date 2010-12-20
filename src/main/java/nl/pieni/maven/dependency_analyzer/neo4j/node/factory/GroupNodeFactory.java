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
import nl.pieni.maven.dependency_analyzer.enums.ArtifactRelations;
import nl.pieni.maven.dependency_analyzer.neo4j.node.GroupNodeDecorator;
import nl.pieni.maven.dependency_analyzer.node.GroupNode;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.logging.Log;
import org.jetbrains.annotations.NotNull;
import org.neo4j.graphdb.Node;

import static nl.pieni.maven.dependency_analyzer.neo4j.enums.NodeProperties.GROUP_ID;

/**
 * GroupNodeFactory
 */
public class GroupNodeFactory extends AbstractNodeFactory<GroupNode> {


    /**
     * {@inheritDoc}
     */
    public GroupNodeFactory(DependencyDatabase database, DependencyDatabaseSearcher<Node> searcher, final Log logger) {
        super(database, searcher, logger);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected GroupNode create(@NotNull final Dependency dependency) {
        getDatabase().startTransaction();
        Node node = getDatabase().createNode();
        GroupNode groupNode = new GroupNodeDecorator(node, dependency);
        getSearcher().indexOnProperty(node, GROUP_ID);
        LOGGER.info("Create GroupNode: " + node);
        getDatabase().stopTransaction();
        return groupNode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int insert(@NotNull final Dependency dependency) {
        int nodeCount = 0;
        GroupNodeDecorator groupNode = (GroupNodeDecorator)getSearcher().findGroupNode(dependency);
        if (groupNode == null) {
            groupNode = (GroupNodeDecorator)create(dependency);
            nodeCount++;
            getDatabase().startTransaction();
            getDatabase().getDatabase().getReferenceNode().createRelationshipTo(groupNode, ArtifactRelations.has);

            LOGGER.info("Created relation " + ArtifactRelations.has + "between referenceNode and " + groupNode);

            getDatabase().stopTransaction();
        }
        return nodeCount;
    }

}
