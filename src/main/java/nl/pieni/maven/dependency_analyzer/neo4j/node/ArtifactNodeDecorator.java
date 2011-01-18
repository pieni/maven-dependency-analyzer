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

package nl.pieni.maven.dependency_analyzer.neo4j.node;

import nl.pieni.maven.dependency_analyzer.neo4j.enums.ArtifactRelations;
import nl.pieni.maven.dependency_analyzer.neo4j.enums.NodeProperties;
import nl.pieni.maven.dependency_analyzer.neo4j.enums.NodeType;
import nl.pieni.maven.dependency_analyzer.node.ArtifactNode;
import nl.pieni.maven.dependency_analyzer.node.GroupNode;
import org.apache.maven.model.Dependency;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

/**
 * ArtifactNode decorator
 */
public class ArtifactNodeDecorator extends AbstractNodeDecorator implements ArtifactNode {

    /**
     * {@inheritDoc}
     */
    public ArtifactNodeDecorator(Node node, Dependency dependency) {
        super(node, dependency);
        if (hasProperty(NodeProperties.NODE_TYPE)) {
            return;
        }
        setProperty(NodeProperties.NODE_TYPE, NodeType.ArtifactNode.name());
    }

    /**
     * {@inheritDoc}
     */
    public ArtifactNodeDecorator(Node node) {
        super(node);
        if (!getProperty(NodeProperties.NODE_TYPE).equals(NodeType.ArtifactNode.name())) {
            throw new IllegalArgumentException("node " + node.getId() + " is not a " + NodeType.ArtifactNode);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDependency(Dependency dependency) {
        if (hasProperty(NodeProperties.ARTIFACT_ID) && hasProperty(NodeProperties.ARTIFACT_TYPE)) {
            return;
        }
        setProperty(NodeProperties.ARTIFACT_ID, dependency.getArtifactId());
        setProperty(NodeProperties.ARTIFACT_TYPE, dependency.getType());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getArtifactId() {
        return (String) getProperty(NodeProperties.ARTIFACT_ID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getType() {
        return (String) getProperty(NodeProperties.ARTIFACT_TYPE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GroupNode getParent() {
        Iterable<Relationship> hasRelations = getRelationships(ArtifactRelations.has, Direction.INCOMING);
        for (Relationship relationship : hasRelations) {
            Node relationNode = relationship.getOtherNode(this);
            return new GroupNodeDecorator(relationNode);
        }

        throw new IllegalArgumentException("Database inconsistent" + this.toString() + " has no parent");
    }
}
