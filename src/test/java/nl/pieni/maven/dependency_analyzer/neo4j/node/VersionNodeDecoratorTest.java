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

import nl.pieni.maven.dependency_analyzer.enums.ArtifactRelations;
import nl.pieni.maven.dependency_analyzer.neo4j.enums.NodeProperties;
import nl.pieni.maven.dependency_analyzer.neo4j.enums.NodeType;
import nl.pieni.maven.dependency_analyzer.node.ArtifactNode;
import org.apache.maven.model.Dependency;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.JUnit4;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import java.util.Iterator;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test of the VersionNodeDecorator
 */
public class VersionNodeDecoratorTest {

    private Node versionNode;
    private Dependency dependency;

    @Before
    public void setUp() throws Exception {
        dependency = mock(Dependency.class);
        when(dependency.getVersion()).thenReturn("1.0");

        versionNode = mock(Node.class);
        when(versionNode.getId()).thenReturn(1L);
        when(versionNode.getProperty(NodeProperties.NODE_TYPE)).thenReturn(NodeType.VersionNode);
        //To avoid a null pointer in the toString method.
        when(versionNode.getPropertyKeys()).thenReturn(new Iterable<String>() {
            @Override
            public Iterator<String> iterator() {
                return new Iterator<String>() {
                    @Override
                    public boolean hasNext() {
                        return false;
                    }

                    @Override
                    public String next() {
                        return null;
                    }

                    @Override
                    public void remove() {
                        //Intentionally left blank.
                    }
                };
            }
        });
    }


    @Test
    public void constructor2Arguments() {
        new VersionNodeDecorator(versionNode, dependency);
        verify(versionNode).setProperty(NodeProperties.NODE_TYPE, NodeType.VersionNode);
        verify(versionNode).setProperty(NodeProperties.VERSION, "1.0");
    }

    @Test
    public void constructor1Argument() {
        when(versionNode.getProperty(NodeProperties.NODE_TYPE)).thenReturn(NodeType.VersionNode);
        new VersionNodeDecorator(versionNode, dependency);
        verify(versionNode).setProperty(NodeProperties.NODE_TYPE, NodeType.VersionNode);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor1ArgumentIllegalArgument() {
        when(versionNode.getProperty(NodeProperties.NODE_TYPE)).thenReturn(NodeType.ArtifactNode);
        new VersionNodeDecorator(versionNode);
    }

    @Test
    public void setDependencyTest() {
        VersionNodeDecorator decorator = new VersionNodeDecorator(versionNode);
        decorator.setDependency(dependency);
        verify(versionNode).getProperty(NodeProperties.NODE_TYPE);
        verify(versionNode).setProperty(NodeProperties.VERSION, "1.0");
    }

    @Test
    public void getVersion() {
        VersionNodeDecorator decorator = new VersionNodeDecorator(versionNode);
        when(versionNode.getProperty(NodeProperties.VERSION)).thenReturn("1.0");
        assertEquals("1.0", decorator.getVersion());
    }

    @Test
    public void getParent() {
        VersionNodeDecorator decorator = new VersionNodeDecorator(versionNode);

        Node parentNode = mock(Node.class);
        when(parentNode.getProperty(NodeProperties.NODE_TYPE)).thenReturn(NodeType.ArtifactNode);
        when(parentNode.getId()).thenReturn(99L);
        Iterable<Relationship> parentRelations = mock(Iterable.class);
        when(versionNode.getRelationships(ArtifactRelations.version, Direction.INCOMING)).thenReturn(parentRelations);
        Relationship relation = mock(Relationship.class);
        Iterator<Relationship> iter = mock(Iterator.class);
        when(parentRelations.iterator()).thenReturn(iter);
        when(iter.hasNext()).thenReturn(true);
        when(iter.next()).thenReturn(relation);
        //TODO Figure out why this only works with the any(...) as argument, it should be versionNode;
        when(relation.getOtherNode(any(Node.class))).thenReturn(parentNode);

        ArtifactNodeDecorator artifactNode = (ArtifactNodeDecorator)decorator.getParent();
        assertEquals(99L, artifactNode.getId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getIllegalParent() {
        VersionNodeDecorator decorator = new VersionNodeDecorator(versionNode);

        Node parentNode = mock(Node.class);
        when(parentNode.getProperty(NodeProperties.NODE_TYPE)).thenReturn(NodeType.GroupNode);
        when(parentNode.getId()).thenReturn(99L);
        Iterable<Relationship> parentRelations = mock(Iterable.class);
        when(versionNode.getRelationships(ArtifactRelations.version, Direction.INCOMING)).thenReturn(parentRelations);
        Iterator<Relationship> iter = mock(Iterator.class);
        when(parentRelations.iterator()).thenReturn(iter);
        when(iter.hasNext()).thenReturn(false);

        decorator.getParent();
    }
}
