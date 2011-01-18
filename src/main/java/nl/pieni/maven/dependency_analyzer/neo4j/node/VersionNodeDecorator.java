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
import nl.pieni.maven.dependency_analyzer.node.VersionNode;
import org.apache.maven.model.Dependency;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

/**
 * VersionNode decorator
 */
public class VersionNodeDecorator extends AbstractNodeDecorator implements VersionNode {


    /**
     * @inheritDoc
     */
    public VersionNodeDecorator(Node node, Dependency dependency) {
        super(node, dependency);
        if (hasProperty(NodeProperties.NODE_TYPE)) {
            return;
        }
        setProperty(NodeProperties.NODE_TYPE, NodeType.VersionNode.name());
    }


    /**
     * @inheritDoc
     */
    public VersionNodeDecorator(Node node) {
        super(node);
        if (!getProperty(NodeProperties.NODE_TYPE).equals(NodeType.VersionNode.name())) {
            throw new IllegalArgumentException("node " + node.getId() + " is not a " + NodeType.VersionNode);
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public void setDependency(Dependency dependency) {
        if (hasProperty(NodeProperties.VERSION)) {
            return;
        }
        setProperty(NodeProperties.VERSION, dependency.getVersion());
    }

    /**
     * @inheritDoc
     */
    @Override
    public String getVersion() {
        return (String) getProperty(NodeProperties.VERSION);
    }

    /**
     * @inheritDoc
     */
    @Override
    public ArtifactNode getParent() {
        Iterable<Relationship> parentRelations = getRelationships(ArtifactRelations.version, Direction.INCOMING);
        for (Relationship parentRelation : parentRelations) {
            Node parent = parentRelation.getOtherNode(this);
            return new ArtifactNodeDecorator(parent);
        }

        throw new IllegalArgumentException("Database inconsistent" + this.toString() + " has no parent");
    }

}
