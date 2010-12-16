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
 * Group Node decorator tests
 */
public class GroupNodeDecoratorTest {
    private Node groupNode;
    private Dependency dependency;

    @Before
    public void setUp() throws Exception {
        dependency = mock(Dependency.class);
        when(dependency.getVersion()).thenReturn("1.0");
        when(dependency.getArtifactId()).thenReturn("artifactID");
        when(dependency.getType()).thenReturn("jar");

        groupNode= mock(Node.class);
        when(groupNode.getId()).thenReturn(1L);
        when(groupNode.getProperty(NodeProperties.NODE_TYPE)).thenReturn(NodeType.GroupNode);
        //To avoid a null pointer in the toString method.
        when(groupNode.getPropertyKeys()).thenReturn(new Iterable<String>() {
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
        new GroupNodeDecorator(groupNode, dependency);
        verify(groupNode).setProperty(NodeProperties.NODE_TYPE, NodeType.GroupNode);
        verify(groupNode).setProperty(NodeProperties.GROUP_ID, dependency.getGroupId());
    }

    @Test
    public void constructor1Argument() {
        new GroupNodeDecorator(groupNode);
        verify(groupNode).getProperty(NodeProperties.NODE_TYPE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor1ArgumentIllegalArgument() {
        when(groupNode.getProperty(NodeProperties.NODE_TYPE)).thenReturn(NodeType.VersionNode);
        new GroupNodeDecorator(groupNode);
    }
    @Test
    public void setDependencyTest() {
       GroupNodeDecorator decorator = new GroupNodeDecorator(groupNode);
        decorator.setDependency(dependency);
        verify(dependency).getGroupId();
        verify(groupNode).getProperty(NodeProperties.NODE_TYPE);
        verify(groupNode).setProperty(NodeProperties.GROUP_ID, dependency.getGroupId());
    }


    @Test
    public void getGroupId() {
        GroupNodeDecorator decorator = new GroupNodeDecorator(groupNode);
        verify(groupNode).getProperty(NodeProperties.NODE_TYPE);
        decorator.getGroupId();
        verify(groupNode).getProperty(NodeProperties.GROUP_ID);
    }
}
