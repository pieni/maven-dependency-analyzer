/*
 * Copyright (c) 2010 Pieter van der Meer (pieter@pieni.nl)
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
import nl.pieni.maven.dependency_analyzer.enums.ArtifactRelations;
import nl.pieni.maven.dependency_analyzer.enums.DependencyScopeRelations;
import nl.pieni.maven.dependency_analyzer.neo4j.enums.NodeProperties;
import nl.pieni.maven.dependency_analyzer.neo4j.node.ArtifactNodeDecorator;
import nl.pieni.maven.dependency_analyzer.neo4j.node.GroupNodeDecorator;
import nl.pieni.maven.dependency_analyzer.neo4j.node.VersionNodeDecorator;
import nl.pieni.maven.dependency_analyzer.node.ArtifactNode;
import nl.pieni.maven.dependency_analyzer.node.GroupNode;
import nl.pieni.maven.dependency_analyzer.node.VersionNode;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.logging.Log;
import org.jetbrains.annotations.NotNull;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ReturnableEvaluator;
import org.neo4j.graphdb.StopEvaluator;
import org.neo4j.graphdb.Traverser;
import org.neo4j.index.IndexService;
import org.neo4j.index.lucene.LuceneIndexService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Searcher for the database
 */
public class DependencyDatabaseSearcherImpl implements DependencyDatabaseSearcher<Node> {

    private final Log LOGGER;
    private IndexService index;

    /**
     * Default constructor
     *
     * @param LOGGER   the logger
     * @param database the database
     */
    public DependencyDatabaseSearcherImpl(Log LOGGER, DependencyDatabase<GraphDatabaseService, Node> database) {
        this.LOGGER = LOGGER;
        this.index = new LuceneIndexService(database.getDatabase());

    }


    /**
     * {@inheritDoc}
     */
    @Override
    public VersionNode findVersionNode(@NotNull final Dependency dependency) {
        ArtifactNodeDecorator artifactNode = (ArtifactNodeDecorator) findArtifactNode(dependency);
        Traverser traverser = artifactNode.traverse(Traverser.Order.BREADTH_FIRST, StopEvaluator.DEPTH_ONE, ReturnableEvaluator.ALL_BUT_START_NODE, ArtifactRelations.version, Direction.OUTGOING);
        for (Node node : traverser) {
            if (node.getProperty(NodeProperties.VERSION).equals(dependency.getVersion())) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Found versionNode for dependency: " + dependency);
                }
                return new VersionNodeDecorator(node, dependency);
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ArtifactNode findArtifactNode(@NotNull final Dependency dependency) {
        GroupNodeDecorator groupNode = (GroupNodeDecorator) findGroupNode(dependency);
        if (groupNode == null) {
            LOGGER.error("Unable to find groupNode for " + dependency);
            return null;
        }
        Traverser traverser = groupNode.traverse(Traverser.Order.BREADTH_FIRST, StopEvaluator.DEPTH_ONE, ReturnableEvaluator.ALL_BUT_START_NODE, ArtifactRelations.has, Direction.OUTGOING);
        for (Node node : traverser) {
            if (node.getProperty(NodeProperties.ARTIFACT_ID).equals(dependency.getArtifactId())) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Found artifactNode for artifact: " + dependency);
                }
                return new ArtifactNodeDecorator(node, dependency);
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GroupNode findGroupNode(@NotNull final Dependency dependency) {
        String key = NodeProperties.GROUP_ID;
        Node node = index.getSingleNode(key, dependency.getGroupId());
        if (node == null) {
            return null;
        }
        return new GroupNodeDecorator(node, dependency);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<VersionNode> getVersionNodes(Dependency dependency) {
        List<VersionNode> versionNodes = new ArrayList<VersionNode>();
        ArtifactNode artifactNode = findArtifactNode(dependency);
        if (artifactNode == null) {
            return versionNodes;
        }

        ArtifactNodeDecorator node = (ArtifactNodeDecorator) artifactNode;
        Iterable<Relationship> versions = node.getRelationships(ArtifactRelations.version, Direction.OUTGOING);
        for (Relationship relationship : versions) {
            Node versionNode = relationship.getOtherNode(node);
            versionNodes.add(new VersionNodeDecorator(versionNode));
        }

        return versionNodes;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Map<DependencyScopeRelations, List<ArtifactNode>> getDependingArtifacts(Dependency dependency) {
        Map<DependencyScopeRelations, List<ArtifactNode>> artifactNodeMap = new HashMap<DependencyScopeRelations, List<ArtifactNode>>();

        ArtifactNodeDecorator artifactNode = (ArtifactNodeDecorator) findArtifactNode(dependency);
        if (artifactNode == null) {
            return artifactNodeMap;
        }

        DependencyScopeRelations[] relations = DependencyScopeRelations.values();
        for (DependencyScopeRelations relation : relations) {
            List<ArtifactNode> artifactNodes = new ArrayList<ArtifactNode>();
            artifactNodeMap.put(relation, artifactNodes);
            Iterable<Relationship> scopeRelations = artifactNode.getRelationships(relation, Direction.INCOMING);
            for (Relationship scopeRelation : scopeRelations) {
                Node scopeNode = scopeRelation.getOtherNode(artifactNode);
                artifactNodes.add(new ArtifactNodeDecorator(scopeNode));
            }
        }

        return artifactNodeMap;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<VersionNode, List<VersionNode>> getVersionDependencies(Dependency dependency) {
        Map<VersionNode, List<VersionNode>> versionNodeListMap = new HashMap<VersionNode, List<VersionNode>>();

        ArtifactNodeDecorator artifactNode = (ArtifactNodeDecorator) findArtifactNode(dependency);
        if (artifactNode == null) {
            return versionNodeListMap;
        }

        Iterable<Relationship> availableVersionRelations = artifactNode.getRelationships(ArtifactRelations.version, Direction.OUTGOING);

        for (Relationship relationship : availableVersionRelations) {
            VersionNodeDecorator versionNode = new VersionNodeDecorator(relationship.getOtherNode(artifactNode));
            List<VersionNode> versionNodes = new ArrayList<VersionNode>();
            versionNodeListMap.put(versionNode, versionNodes);


            Iterable<Relationship> versionRelations = versionNode.getRelationships(ArtifactRelations.VersionsDependency, Direction.INCOMING);
            for (Relationship versionRelation : versionRelations) {
                Node relationNode = versionRelation.getOtherNode(versionNode);
                versionNodes.add(new VersionNodeDecorator(relationNode));
            }
        }
        return versionNodeListMap;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void indexOnProperty(@NotNull final Node node, final String key) {
        index.index(node, key, node.getProperty(key));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdownSearcher() {
        index.shutdown();
    }

}
