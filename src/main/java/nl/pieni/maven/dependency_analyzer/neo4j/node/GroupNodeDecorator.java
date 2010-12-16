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

import nl.pieni.maven.dependency_analyzer.neo4j.enums.NodeProperties;
import nl.pieni.maven.dependency_analyzer.neo4j.enums.NodeType;
import nl.pieni.maven.dependency_analyzer.node.GroupNode;
import org.apache.maven.model.Dependency;
import org.neo4j.graphdb.Node;

/**
 * GroupNode decorator
 */
public class GroupNodeDecorator extends AbstractNodeDecorator implements GroupNode {

    /**
     * @inheritDoc
     */
    public GroupNodeDecorator(Node node, Dependency dependency) {
        super(node, dependency);
        setProperty(NodeProperties.NODE_TYPE, NodeType.GroupNode);
    }

    /**
     * @inheritDoc
     */
    public GroupNodeDecorator(Node node) {
        super(node);
        if (node.getProperty(NodeProperties.NODE_TYPE) != NodeType.GroupNode) {
            throw new IllegalArgumentException("node " + node.getId() + " is not a " + NodeType.GroupNode);
        }
    }


    /**
     * @inheritDoc
     */
    @Override
    public void setDependency(Dependency dependency) {
        setProperty(NodeProperties.GROUP_ID, dependency.getGroupId());
    }

    /**
     * @inheritDoc
     */
    @Override
    public String getGroupId() {
        return (String)super.getProperty(NodeProperties.GROUP_ID);
    }
}
