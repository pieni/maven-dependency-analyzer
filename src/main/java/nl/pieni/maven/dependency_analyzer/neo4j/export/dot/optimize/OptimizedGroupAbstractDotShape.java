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

package nl.pieni.maven.dependency_analyzer.neo4j.export.dot.optimize;

import nl.pieni.maven.dependency_analyzer.neo4j.enums.NodeProperties;
import nl.pieni.maven.dependency_analyzer.neo4j.export.dot.shapes.AbstractDotShape;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import java.util.Set;

/**
 */
public class OptimizedGroupAbstractDotShape extends AbstractDotShape {

    private Node startNode;

    public OptimizedGroupAbstractDotShape(Node node, Set<Relationship> relations) {
        super(node, relations);
    }

    public Node getStartNode() {
        return startNode;
    }

    public void setStartNode(Node startNode) {
        this.startNode = startNode;
    }

    @Override
    public String getId() {
        return "GN" + getNode().getId();
    }

    public boolean startsWith(Node shape) {
        return startNode.getId() == shape.getId();
    }

    @Override
    public String getLabel() {
        String startGroupId = startNode.getProperty(NodeProperties.GROUP_ID).toString();
        String endGroupId = getNode().getProperty(NodeProperties.GROUP_ID).toString();

        if (endGroupId.length() < startGroupId.length()) {
            throw new IllegalArgumentException("End GroupId cannot be shorter then the start GroupId (startNode=" + startNode + " endNode=" + getNode());
        }

        return endGroupId.substring(startGroupId.length() + 1);
    }
}
