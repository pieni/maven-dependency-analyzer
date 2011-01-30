/*
 * Copyright (c) 2011 Pieter van der Meer (pieter@pieni.nl)
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

package nl.pieni.maven.dependency_analyzer.neo4j.export.dot;

import nl.pieni.maven.dependency_analyzer.filter.GAVIncludeFilter;
import nl.pieni.maven.dependency_analyzer.neo4j.enums.ArtifactRelations;
import nl.pieni.maven.dependency_analyzer.neo4j.enums.NodeProperties;
import nl.pieni.maven.dependency_analyzer.neo4j.enums.NodeType;
import nl.pieni.maven.dependency_analyzer.neo4j.node.ArtifactNodeDecorator;
import nl.pieni.maven.dependency_analyzer.neo4j.node.GroupNodeDecorator;
import nl.pieni.maven.dependency_analyzer.neo4j.node.VersionNodeDecorator;
import nl.pieni.maven.dependency_analyzer.node.ArtifactNode;
import nl.pieni.maven.dependency_analyzer.node.GroupNode;
import nl.pieni.maven.dependency_analyzer.node.VersionNode;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import java.util.List;

/**
 * Pattern matcher for for Nodes.
 * depends of the {@link GAVIncludeFilter} for its processing.
 * Determines if a {@link Node} must be included in the file exported
 **/
class IncludeFilterPatternMatcher {
    private final GAVIncludeFilter gavIncludeFilter;

    /**
     * Default constructor
     * @param includeFilterPatterns the patters to match {@link GAVIncludeFilter} for format
     */
    public IncludeFilterPatternMatcher(List<String> includeFilterPatterns) {
        this.gavIncludeFilter = new GAVIncludeFilter(includeFilterPatterns);
    }

    /**
     * Test to see of the provided node must be included
     * @param node the Node
     * @return true when it must be included
     */
    public boolean include(Node node) {
        if (!node.hasProperty(NodeProperties.NODE_TYPE)) {
            throw new IllegalArgumentException("Matcher can not be called with a non Dependency node");
        }

        NodeType type = NodeType.fromString((String) node.getProperty(NodeProperties.NODE_TYPE));

        switch (type) {
            case VersionNode:
                return matchVersionNode(node);
            case ArtifactNode:
                return matchArtifactNode(node);
            case GroupNode:
                return matchGroupNode(node);
        }

        return false;
    }

    /**
     * Perform the matching for an GroupNode
     * @param node the artifact node
     * @return true when matches
     */
    private boolean matchGroupNode(Node node) {
        GroupNodeDecorator groupNode = new GroupNodeDecorator(node);
        String gav = groupNode.getGroupId();
        if (gavIncludeFilter.filter(gav)) {
            return true;
        }

        return hasMatchFurtherDown(groupNode);

    }

    /**
     * Due to the structure of the DB "nl.pieni.maven" is stored as: nl -> nl.pieni -> nl.pieni.maven
     * So a false match needs some more work to be false
     * @param groupNodeDecorator the GroupNode
     * @return true when a match is found
     */
    private boolean hasMatchFurtherDown(GroupNodeDecorator groupNodeDecorator) {

        Iterable<Relationship> relationships = groupNodeDecorator.getRelationships(ArtifactRelations.has, Direction.OUTGOING);
        for (Relationship relationship : relationships) {
            Node otherNode = relationship.getOtherNode(groupNodeDecorator);
            if (NodeType.ArtifactNode == NodeType.fromString((String)otherNode.getProperty(NodeProperties.NODE_TYPE))) {
                return matchArtifactNode(otherNode);
            }

            return matchGroupNode(otherNode);
        }
        return false;
    }

    /**
     * Match an ArtifactNode
     * @param node the Node
     * @return true when matched
     */
    private boolean matchArtifactNode(Node node) {
        ArtifactNode artifactNode = new ArtifactNodeDecorator(node);
        GroupNode groupNode = artifactNode.getParent();
        String gav = groupNode.getGroupId() + ":" + artifactNode.getArtifactId();
        return gavIncludeFilter.filter(gav);
    }

    /**
     * Match a version node
     * @param node the Node
     * @return true when matched
     */
    private boolean matchVersionNode(Node node) {
        VersionNode versionNode = new VersionNodeDecorator(node);
        ArtifactNode artifactNode = versionNode.getParent();
        GroupNode groupNode = artifactNode.getParent();
        String gav = groupNode.getGroupId() + ":" + artifactNode.getArtifactId() + ":" + versionNode.getVersion();
        return gavIncludeFilter.filter(gav);
    }
}