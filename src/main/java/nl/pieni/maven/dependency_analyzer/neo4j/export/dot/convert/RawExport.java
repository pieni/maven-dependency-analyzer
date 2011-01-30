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

package nl.pieni.maven.dependency_analyzer.neo4j.export.dot.convert;

import nl.pieni.maven.dependency_analyzer.dot.NodeWriter;
import nl.pieni.maven.dependency_analyzer.neo4j.enums.NodeProperties;
import nl.pieni.maven.dependency_analyzer.neo4j.enums.NodeType;
import nl.pieni.maven.dependency_analyzer.neo4j.node.ArtifactNodeDecorator;
import nl.pieni.maven.dependency_analyzer.neo4j.node.GroupNodeDecorator;
import nl.pieni.maven.dependency_analyzer.neo4j.node.VersionNodeDecorator;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: pieter
 * Date: 30-1-11
 * Time: 20:00
 * To change this template use File | Settings | File Templates.
 */
public class RawExport {
    private final NodeWriter nodeWriter;
    private final Map<Node, Set<Relationship>> exportNodeMap;

    public RawExport(NodeWriter nodeWriter, Map<Node, Set<Relationship>> exportNodeMap) {
        this.nodeWriter = nodeWriter;
        this.exportNodeMap = exportNodeMap;
    }

    public void writeDotFile() throws IOException {
        writeNodes();

        writeNodeRelations();

        nodeWriter.close();
    }

    private void writeNodeRelations() throws IOException {
        Set<Node> nodes = exportNodeMap.keySet();
        for (Node startNode : nodes) {
            Set<Relationship> relationshipSet = exportNodeMap.get(startNode);
            for (Relationship relationship : relationshipSet) {
                nodeWriter.writeNode2NodeRelation(startNode, relationship.getEndNode(), relationship.getType());
            }
        }
    }

    private void writeNodes() throws IOException {
        for (Node node : exportNodeMap.keySet()) {
            if (node.hasProperty(NodeProperties.NODE_TYPE)) {
                NodeType type = NodeType.fromString(node.getProperty(NodeProperties.NODE_TYPE).toString());
                switch (type) {
                    case ArtifactNode:
                        nodeWriter.writeNode(new ArtifactNodeDecorator(node));
                        break;
                    case GroupNode:
                        nodeWriter.writeNode(new GroupNodeDecorator(node));
                        break;
                    case VersionNode:
                        nodeWriter.writeNode(new VersionNodeDecorator(node));
                }
            } else {
                if (node.getId() == 0) {
                    nodeWriter.writeRootNode(node);
                }
            }
        }
    }
}
