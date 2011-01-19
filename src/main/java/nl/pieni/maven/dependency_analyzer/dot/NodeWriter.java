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

package nl.pieni.maven.dependency_analyzer.dot;

import nl.pieni.maven.dependency_analyzer.neo4j.node.ArtifactNodeDecorator;
import nl.pieni.maven.dependency_analyzer.neo4j.node.GroupNodeDecorator;
import nl.pieni.maven.dependency_analyzer.neo4j.node.VersionNodeDecorator;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

import java.io.IOException;

/**
 * Interface to the NodeWriter
 */
public interface NodeWriter {
    /**
     * Close the file
     * @throws IOException in case of error
     */
    void close() throws IOException;

    /**
     * Write a Node
     * @param node the Node
     * @throws IOException in case of error
     */
    void writeNode(VersionNodeDecorator node) throws IOException;

    /**
     * Write the root or reference node
     * @param node the Node
     * @throws IOException in case of error
     */
    void writeRootNode(Node node) throws IOException;

    /**
     * Write an ArtifactNode
     * @param node the Node
     * @throws IOException in case of error
     */
    void writeNode(ArtifactNodeDecorator node) throws IOException;

    /**
     * Write a GRoupNode
     * @param node the Node
     * @throws IOException in case of error
     */
    void writeNode(GroupNodeDecorator node) throws IOException;

    /**
     * Write a Relation
     * @param relationship the relation
     * @throws IOException in case of error
     */
    void writeRelation(Relationship relationship) throws IOException;

    /**
     * Write a Node that requires some tweaking. i.e. not the entire path must be printed in the
     * Node. A part (the lastGroupNode) is already printed.
     * @param currentNode the node to print
     * @param lastGroupNode the previous node in the graph
     * @throws IOException un case of error
     */
    void writeNode(GroupNodeDecorator currentNode, GroupNodeDecorator lastGroupNode) throws IOException;

    /**
     * Write the top level nodes. i.e. the references from the root (refnode) to the ones
     * directly below it.
     * @param refNode the refNode
     * @param childNode the relation.
     * @throws IOException in case of error
     */
    void writeReferenceRelation(Node refNode, Node childNode) throws IOException;
}
