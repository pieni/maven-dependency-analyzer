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
import org.apache.maven.model.Dependency;
import org.junit.Before;
import org.junit.Test;
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
 * Artifact Node Decorator test
 */
public class ArtifactNodeDecoratorTest {

    private Node artifactNode;
    private Dependency dependency;

    @Before
    public void setUp() throws Exception {
        dependency = mock(Dependency.class);
        when(dependency.getVersion()).thenReturn("1.0");
        when(dependency.getArtifactId()).thenReturn("artifactID");
        when(dependency.getType()).thenReturn("jar");

        artifactNode = mock(Node.class);
        when(artifactNode.getId()).thenReturn(1L);
        when(artifactNode.getProperty(NodeProperties.NODE_TYPE)).thenReturn(NodeType.ArtifactNode.name());
        when(artifactNode.getProperty(NodeProperties.ARTIFACT_TYPE)).thenReturn("jar");
        //To avoid a null pointer in the toString method.
        when(artifactNode.getPropertyKeys()).thenReturn(new Iterable<String>() {
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
        new ArtifactNodeDecorator(artifactNode, dependency);
        verify(artifactNode).setProperty(NodeProperties.NODE_TYPE, NodeType.ArtifactNode.name());
        verify(dependency).getArtifactId();
        verify(artifactNode).setProperty(NodeProperties.ARTIFACT_ID, "artifactID");
        verify(artifactNode).setProperty(NodeProperties.ARTIFACT_TYPE, "jar");
    }

    @Test
    public void constructor1Argument() {
        new ArtifactNodeDecorator(artifactNode);
        verify(artifactNode).getProperty(NodeProperties.NODE_TYPE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor1ArgumentIllegalArgument() {
        when(artifactNode.getProperty(NodeProperties.NODE_TYPE)).thenReturn(NodeType.VersionNode);
        new ArtifactNodeDecorator(artifactNode);
    }

    @Test
    public void setDependencyTest() {
        ArtifactNodeDecorator decorator = new ArtifactNodeDecorator(artifactNode);
        decorator.setDependency(dependency);
        verify(artifactNode).getProperty(NodeProperties.NODE_TYPE);
        verify(artifactNode).setProperty(NodeProperties.ARTIFACT_ID, "artifactID");
        verify(artifactNode).setProperty(NodeProperties.ARTIFACT_TYPE, "jar");
    }


    @Test
    public void getArtifactId() {
        ArtifactNodeDecorator decorator = new ArtifactNodeDecorator(artifactNode);
        verify(artifactNode).getProperty(NodeProperties.NODE_TYPE);
        decorator.getArtifactId();
        verify(artifactNode).getProperty(NodeProperties.ARTIFACT_ID);
    }

    @Test
    public void getType() {
        ArtifactNodeDecorator decorator = new ArtifactNodeDecorator(artifactNode);
        verify(artifactNode).getProperty(NodeProperties.NODE_TYPE);
        assertEquals("jar", decorator.getType());
        verify(artifactNode).getProperty(NodeProperties.ARTIFACT_TYPE);

    }

    @Test
    public void getParent() {
        ArtifactNodeDecorator decorator = new ArtifactNodeDecorator(artifactNode);

        Node parentNode = mock(Node.class);
        when(parentNode.getProperty(NodeProperties.NODE_TYPE)).thenReturn(NodeType.GroupNode.name());
        when(parentNode.getId()).thenReturn(99L);
        @SuppressWarnings("unchecked")
        Iterable<Relationship> parentRelations = mock(Iterable.class);
        when(artifactNode.getRelationships(ArtifactRelations.has, Direction.INCOMING)).thenReturn(parentRelations);
        Relationship relation = mock(Relationship.class);
        @SuppressWarnings("unchecked")
        Iterator<Relationship> iter = mock(Iterator.class);
        when(parentRelations.iterator()).thenReturn(iter);
        when(iter.hasNext()).thenReturn(true);
        when(iter.next()).thenReturn(relation);
        //Figure out why this only works with the any(...) as argument, it should be versionNode;
        when(relation.getOtherNode(any(Node.class))).thenReturn(parentNode);

        GroupNodeDecorator groupNode = (GroupNodeDecorator) decorator.getParent();
        assertEquals(99L, groupNode.getId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getIllegalParent() {
        ArtifactNodeDecorator decorator = new ArtifactNodeDecorator(artifactNode);

        Node parentNode = mock(Node.class);
        when(parentNode.getProperty(NodeProperties.NODE_TYPE)).thenReturn(NodeType.GroupNode);
        when(parentNode.getId()).thenReturn(99L);
        @SuppressWarnings("unchecked")
        Iterable<Relationship> parentRelations = mock(Iterable.class);
        when(artifactNode.getRelationships(ArtifactRelations.has, Direction.INCOMING)).thenReturn(parentRelations);
        @SuppressWarnings("unchecked")
        Iterator<Relationship> iter = mock(Iterator.class);
        when(parentRelations.iterator()).thenReturn(iter);
        when(iter.hasNext()).thenReturn(false);

        decorator.getParent();
    }
}
