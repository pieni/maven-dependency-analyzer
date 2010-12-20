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
import nl.pieni.maven.dependency_analyzer.neo4j.node.ArtifactNodeDecorator;
import nl.pieni.maven.dependency_analyzer.neo4j.node.VersionNodeDecorator;
import nl.pieni.maven.dependency_analyzer.node.VersionNode;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.logging.Log;
import org.jetbrains.annotations.NotNull;
import org.neo4j.graphdb.*;

import static nl.pieni.maven.dependency_analyzer.neo4j.enums.NodeProperties.VERSION;

/**
 * version Node
 */
public class VersionNodeFactory extends AbstractNodeFactory<VersionNode> {


    /**
     * {@inheritDoc}
     */
    public VersionNodeFactory(DependencyDatabase database, DependencyDatabaseSearcher<Node> searcher, final Log logger) {
        super(database, searcher, logger);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected VersionNode create(@NotNull final Dependency dependency) {
        getDatabase().startTransaction();
        VersionNode versionNode = new VersionNodeDecorator(getDatabase().createNode(), dependency);
        LOGGER.info("Create versionNode: " + versionNode);
        getDatabase().stopTransaction();
        return versionNode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int insert(@NotNull final Dependency dependency) {
        int nodeCount = 0;
        ArtifactNodeDecorator artifactNode = (ArtifactNodeDecorator) getSearcher().findArtifactNode(dependency);
        VersionNodeDecorator versionNode = (VersionNodeDecorator) getSearcher().findVersionNode(dependency);
        if (versionNode == null) {
            versionNode = (VersionNodeDecorator) create(dependency);
            nodeCount++;
            getDatabase().startTransaction();
            artifactNode.createRelationshipTo(versionNode, ArtifactRelations.version);
            LOGGER.info("Created relation " + ArtifactRelations.version + "between " + artifactNode + " and " + versionNode);
            getDatabase().stopTransaction();
        }
        return nodeCount;
    }
}
