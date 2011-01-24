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

package nl.pieni.maven.dependency_analyzer.neo4j.export;

import nl.pieni.maven.dependency_analyzer.dot.NodeShapes.NodeShape;
import nl.pieni.maven.dependency_analyzer.dot.NodeWriter;
import nl.pieni.maven.dependency_analyzer.neo4j.enums.ArtifactRelations;
import nl.pieni.maven.dependency_analyzer.neo4j.node.ArtifactNodeDecorator;
import nl.pieni.maven.dependency_analyzer.neo4j.node.GroupNodeDecorator;
import nl.pieni.maven.dependency_analyzer.neo4j.node.VersionNodeDecorator;
import nl.pieni.maven.dependency_analyzer.test_helpers.SimpleLogger;
import org.apache.maven.plugin.logging.Log;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import java.io.IOException;
import java.io.Writer;

import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Node writer test cases
 */
public class NodeWriterImplTest {

    private NodeWriter writer;
    private Writer outputWriter;

    @Before
    public void before() throws IOException {
        outputWriter = mock(Writer.class);
        Log log = new SimpleLogger();
        this.writer = new NodeWriterImpl(outputWriter, log);
    }


    @Test
    public void testClose() throws Exception {
        writer.close();
        verifyDotFile();
    }

    @Test
    public void testWriteVersionNode() throws Exception {
        VersionNodeDecorator versionNodeDecorator = mock(VersionNodeDecorator.class);
        when(versionNodeDecorator.getId()).thenReturn(1L);
        when(versionNodeDecorator.getVersion()).thenReturn("1.0");
        writer.writeNode(versionNodeDecorator);
        writer.close();
        verifyDotFile();
        verify(outputWriter).write(argThat(new MultiStringMatcher(new String[]{"N1", "1.0", NodeShape.component.toString()})));
    }

    @Test
    public void testWriteRootNode() throws Exception {
        Node node = mock(Node.class);
        when(node.getId()).thenReturn(0L);
        writer.writeRootNode(node);
        writer.close();
        verify(outputWriter).write(argThat(new MultiStringMatcher(new String[]{"N0", "root", NodeShape.box.toString()})));
        verify(outputWriter).write(contains(NodeShape.box.toString()));
    }

    @Test
    public void testWriteArtifactNode() throws Exception {
        ArtifactNodeDecorator node = mock(ArtifactNodeDecorator.class);
        when(node.getId()).thenReturn(1L);
        when(node.getArtifactId()).thenReturn("artifactId");
        writer.writeNode(node);
        writer.close();
        verifyDotFile();
        verify(outputWriter).write(argThat(new MultiStringMatcher(new String[]{"N1", "artifactId", NodeShape.rect.toString()})));
    }

    @Test
    public void testWriteGroupNode() throws Exception {
        GroupNodeDecorator node = mock(GroupNodeDecorator.class);
        when(node.getId()).thenReturn(1L);
        when(node.getGroupId()).thenReturn("nl.pieni.maven");
        writer.writeNode(node);
        writer.close();
        verifyDotFile();
        verify(outputWriter).write(argThat(new MultiStringMatcher(new String[]{"N1", "nl.pieni.maven", NodeShape.folder.toString()})));
    }

    @Test
    public void testWriteRelation() throws Exception {
        Relationship relationship = mock(Relationship.class);
        Node startNode = mock(Node.class);
        Node endNode = mock(Node.class);
        when(relationship.getStartNode()).thenReturn(startNode);
        when(relationship.getEndNode()).thenReturn(endNode);
        when(startNode.getId()).thenReturn(1L);
        when(endNode.getId()).thenReturn(2L);
        when(relationship.getType()).thenReturn(ArtifactRelations.has);
        writer.writeRelation(relationship);
        writer.close();
        verifyDotFile();
        verify(outputWriter).write(argThat(new MultiStringMatcher(new String[]{"N1 -> N2", ArtifactRelations.has.toString()})));
    }

    @Test
    public void testWriteFixedGroupNode() throws Exception {
        GroupNodeDecorator lastNode = mock(GroupNodeDecorator.class);
        GroupNodeDecorator currentNode = mock(GroupNodeDecorator.class);
        when(lastNode.getGroupId()).thenReturn("nl.pieni.maven");
        when(lastNode.getId()).thenReturn(1L);
        when(currentNode.getGroupId()).thenReturn("nl.pieni.maven.dependency_analyzer");
        when(currentNode.getId()).thenReturn(2L);
        writer.writeNode(currentNode, lastNode);
        writer.close();
        verifyDotFile();
        verify(outputWriter).write(argThat(new MultiStringMatcher(new String[]{"N2", "dependency_analyzer", NodeShape.folder.toString()})));
    }

    @Test
    public void testWriteReferenceRelation() throws Exception {
        GroupNodeDecorator groupNode = mock(GroupNodeDecorator.class);
        Node refNode = mock(Node.class);
        when(groupNode.getId()).thenReturn(1L);
        when(refNode.getId()).thenReturn(0L);
        writer.writeNode2NodeRelation(refNode, groupNode);
        writer.close();
        verifyDotFile();
        verify(outputWriter).write(argThat(new MultiStringMatcher(new String[]{"N0 -> N1", ArtifactRelations.has.toString()})));
    }


    @Test
    public void testWriteArtifactNodeTwice() throws Exception {
        ArtifactNodeDecorator node = mock(ArtifactNodeDecorator.class);
        when(node.getId()).thenReturn(1L);
        when(node.getArtifactId()).thenReturn("artifactId");
        writer.writeNode(node);
        writer.writeNode(node);
        writer.close();
        verifyDotFile();
        verify(outputWriter).write(argThat(new MultiStringMatcher(new String[]{"N1", "artifactId", NodeShape.rect.toString()})));
    }

    @Test
    public void testWriteRelationTwice() throws Exception {
        Relationship relationship = mock(Relationship.class);
        Node startNode = mock(Node.class);
        Node endNode = mock(Node.class);
        when(relationship.getStartNode()).thenReturn(startNode);
        when(relationship.getEndNode()).thenReturn(endNode);
        when(startNode.getId()).thenReturn(1L);
        when(endNode.getId()).thenReturn(2L);
        when(relationship.getType()).thenReturn(ArtifactRelations.has);
        writer.writeRelation(relationship);
        writer.writeRelation(relationship);
        writer.close();
        verifyDotFile();
        verify(outputWriter).write(argThat(new MultiStringMatcher(new String[]{"N1 -> N2", ArtifactRelations.has.toString()})));
    }

    private void verifyDotFile() throws IOException {
        verify(outputWriter).write(contains("digraph G"));
        verify(outputWriter).write(contains("}"));
    }


}
