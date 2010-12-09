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

package nl.pieni.maven.dependency_analyzer.neo4j.database.impl;

import nl.pieni.maven.dependency_analyzer.neo4j.enums.ArtifactRelations;
import nl.pieni.maven.dependency_analyzer.neo4j.enums.NodeProperties;
import nl.pieni.maven.dependency_analyzer.neo4j.database.Searcher;
import nl.pieni.maven.dependency_analyzer.neo4j.enums.NodeType;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.logging.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.neo4j.graphdb.*;
import org.neo4j.index.IndexService;

import java.util.ArrayList;
import java.util.List;

/**
 * Searching in the DB
 */
public class SearcherImpl implements Searcher {
    private final IndexService index;
    private final Log LOGGER;

    public SearcherImpl(final IndexService index, final Log LOGGER) {
        this.index = index;
        this.LOGGER = LOGGER;
    }

    @NotNull
    @Override
    public Node findVersionNode(@NotNull final Dependency dependency) {
        Node artifactNode = findArtifactNode(dependency);
        Traverser traverser = artifactNode.traverse(Traverser.Order.BREADTH_FIRST, StopEvaluator.DEPTH_ONE, ReturnableEvaluator.ALL_BUT_START_NODE, ArtifactRelations.version, Direction.OUTGOING);
        for (Node node : traverser) {
            if (node.getProperty(NodeProperties.VERSION).equals(dependency.getVersion())) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Found versionNode for dependency: " + dependency);
                }
                return node;
            }
        }
        throw new IllegalArgumentException("version Node not found" + dependency);
    }

    @Nullable
    @Override
    public Node findArtifactNode(@NotNull final Dependency dependency) {
        Node groupNode = findGroupNode(dependency);
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
                return node;
            }
        }
        return null;
    }

    @Override
    public Node findGroupNode(@NotNull final Dependency dependency) {
        String key = NodeProperties.GROUP_ID;
        return index.getSingleNode(key, dependency.getGroupId());
    }

    public List<Node> pathToReferenceNode(Node node) {
        NodeType nodeType = NodeType.fromString(node.getProperty(NodeProperties.NODE_TYPE).toString());
        Node currentNode = node;
        List<Node> nodeList = new ArrayList<Node>();
        nodeList.add(currentNode);
        switch (nodeType) {
            case VersionNode:
                nodeList = versionToReference(node);
                break;
            case ArtifactNode:
                nodeList = artifactToReference(node);
                break;

            case GroupNode:
                nodeList.add(currentNode);
                break;

        }
        return nodeList;
    }

    private List<Node> artifactToReference(Node node) {
        List<Node> nodeList = new ArrayList<Node>();
        Iterable<Relationship> groups = node.getRelationships(ArtifactRelations.has, Direction.INCOMING);
        for (Relationship group : groups) {
            Node currentNode = group.getOtherNode(node);
            nodeList.add(currentNode);
            break;
        }

        return nodeList;
    }

    private List<Node> versionToReference(Node node) {
        ArrayList<Node> nodeList = new ArrayList<Node>();
        Node currentNode;
        currentNode = node;
        Iterable<Relationship> artifacts = currentNode.getRelationships(ArtifactRelations.version, Direction.INCOMING);
        for (Relationship version : artifacts) {
            currentNode = version.getOtherNode(node);
            nodeList.add(currentNode);
            break;
        }

        nodeList.addAll(artifactToReference(currentNode));
        return nodeList;
    }
}
