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

package nl.pieni.maven.dependency_analyzer.neo4j.export.dot.shapes;

import nl.pieni.maven.dependency_analyzer.dot.NodeShapes.NodeShape;
import nl.pieni.maven.dependency_analyzer.neo4j.enums.NodeProperties;
import nl.pieni.maven.dependency_analyzer.neo4j.enums.NodeType;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import java.util.HashSet;
import java.util.Set;

/**
 * Base class for all dot Shapes
 */
public abstract class AbstractDotShape {

    private final Node node;
    private final Set<Relationship> relations;

    public AbstractDotShape(Node node, Set<Relationship> relations) {
        this.node = node;
        this.relations = relations;
    }

    abstract public String getId();

    public NodeShape getShape() {
         if (node.hasProperty(NodeProperties.NODE_TYPE)) {
            switch (NodeType.fromString(node.getProperty(NodeProperties.NODE_TYPE))) {
                case VersionNode:
                    return NodeShape.component;
                case ArtifactNode:
                    return NodeShape.rect;
                case GroupNode:
                    return NodeShape.folder;
            }
        }
        return NodeShape.box;
    }


    public abstract String getLabel();

    public Node getNode() {
        return node;
    }

    public Set<DotEdge> getEdges() {
        Set<DotEdge> result = new HashSet<DotEdge>();
        for (Relationship relation : relations) {
            result.add(new DotEdge(relation));
        }
        return result;
    }

    @Override
    public String toString() {
        return getId() + " [ label=\"" + getLabel() + "\" shape=\"" + getShape() + "\" ]";
    }

    @Override
    public int hashCode() {
        return node.hashCode();
    }

}
