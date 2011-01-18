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
 * Created by IntelliJ IDEA.
 * User: pieter
 * Date: 15-1-11
 * Time: 23:39
 * To change this template use File | Settings | File Templates.
 */
public interface NodeWriter {
    void close() throws IOException;

    void writeNode(VersionNodeDecorator node) throws IOException;

    void writeRootNode(Node node) throws IOException;

    void writeNode(ArtifactNodeDecorator node) throws IOException;

    void writeNode(GroupNodeDecorator node) throws IOException;

    void writeRelation(Relationship type) throws IOException;

    void writeNode(GroupNodeDecorator groupNodeDecorator, GroupNodeDecorator lastGroupNode) throws IOException;

    void writeReferenceRelation(Node refNode, Node refNodeRelation) throws IOException;
}
