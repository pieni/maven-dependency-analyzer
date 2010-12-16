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

import nl.pieni.maven.dependency_analyzer.enums.DependencyScopeRelations;
import org.apache.maven.model.Dependency;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.ReturnableEvaluator;
import org.neo4j.graphdb.StopEvaluator;
import org.neo4j.graphdb.Traverser;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Abstract node decorator test
 * NOTE: Only the first test is real all others are to ensure coverage and verification all method are properly forwarded.
 */
public class AbstractNodeDecoratorTest {


    private Node node;
    private AbstractNodeDecoratorImpl decorator;

    private class AbstractNodeDecoratorImpl extends AbstractNodeDecorator {

        public AbstractNodeDecoratorImpl(Node node) {
            super(node);
        }

        @Override
        public void setDependency(Dependency dependency) {
            //Left blank
        }
    }

    /**
     * To String conversion of the node
     */
    @Test
    public void toStringTest() {
        Map<String, String> keyValueMap = new HashMap<String, String>() {
            {
                put("key1", "value1");
                put("key2", "value2");
            }
        };
        when(node.getPropertyKeys()).thenReturn(keyValueMap.keySet());
        for (String key : keyValueMap.keySet()) {
            when(node.getProperty(key)).thenReturn(keyValueMap.get(key));
        }
        when(node.getId()).thenReturn(1L);
        assertEquals("Node{ Id = 1 key = key2 value = value2 key = key1 value = value1}", decorator.toString());
    }


    /**
     * Proxy forward test
     */
    @Test
    public void getIdTest() {
        decorator.getId();
        verify(node).getId();
    }


    /**
     * Proxy forward test
     */
    @Test
    public void deleteTest() {
        decorator.delete();
        verify(node).delete();
    }

    /**
     * Proxy forward test
     */
    @Test
    public void getRelationshipsTest() {
        decorator.getRelationships();
        verify(node).getRelationships();
    }

    /**
     * Proxy forward test
     */
    @Test
    public void hasRelationshipTest() {
        decorator.hasRelationship();
        verify(node).hasRelationship();
    }

    /**
     * Proxy forward test
     */
    @Test
    public void getRelationshipsRelationshipTypeTest() {
        decorator.getRelationships(DependencyScopeRelations.compile);
        verify(node).getRelationships(DependencyScopeRelations.compile);
    }

    /**
     * Proxy forward test
     */
    @Test
    public void hasRelationshipRelationshipTypeTest() {
        decorator.hasRelationship(DependencyScopeRelations.compile);
        verify(node).hasRelationship(DependencyScopeRelations.compile);
    }

    /**
     * Proxy forward test
     */
    @Test
    public void getRelationshipsDirectionTest() {
        decorator.getRelationships(Direction.INCOMING);
        verify(node).getRelationships(Direction.INCOMING);
    }

    /**
     * Proxy forward test
     */
    @Test
    public void hasRelationshipDirectionTest() {
        decorator.hasRelationship(Direction.INCOMING);
        verify(node).hasRelationship(Direction.INCOMING);
    }

    /**
     * Proxy forward test
     */
    @Test
    public void getRelationshipsRelationshipTypeDirectionTest() {
        decorator.getRelationships(DependencyScopeRelations.compile, Direction.INCOMING);
        verify(node).getRelationships(DependencyScopeRelations.compile, Direction.INCOMING);
    }

    /**
     * Proxy forward test
     */
    @Test
    public void hasRelationshipRelationshipTypeDirectionTest() {
        decorator.hasRelationship(DependencyScopeRelations.compile, Direction.INCOMING);
        verify(node).hasRelationship(DependencyScopeRelations.compile, Direction.INCOMING);
    }

    /**
     * Proxy forward test
     */
    @Test
    public void getSingleRelationshipRelationshipTypeDirectionTest() {
        decorator.getSingleRelationship(DependencyScopeRelations.compile, Direction.BOTH);
        verify(node).getSingleRelationship(DependencyScopeRelations.compile, Direction.BOTH);
    }

    /**
     * Proxy forward test
     */
    @Test
    public void createRelationshipToNodeRelationshipTypeTest() {
        Node otherNode = mock(Node.class);
        decorator.createRelationshipTo(otherNode, DependencyScopeRelations.compile);
        verify(node).createRelationshipTo(otherNode, DependencyScopeRelations.compile);
    }

    /**
     * Proxy forward test
     */
    @Test
    public void traverseType1Test() {
        decorator.traverse(Traverser.Order.BREADTH_FIRST, StopEvaluator.DEPTH_ONE, ReturnableEvaluator.ALL, DependencyScopeRelations.compile, Direction.BOTH);
        verify(node).traverse(Traverser.Order.BREADTH_FIRST, StopEvaluator.DEPTH_ONE, ReturnableEvaluator.ALL, DependencyScopeRelations.compile, Direction.BOTH);
    }

    /**
     * Proxy forward test
     */
    @Test
    public void traverseType2Test() {
        decorator.traverse(Traverser.Order.BREADTH_FIRST, StopEvaluator.DEPTH_ONE, ReturnableEvaluator.ALL, DependencyScopeRelations.compile, Direction.BOTH, DependencyScopeRelations.provided, Direction.BOTH);
        verify(node).traverse(Traverser.Order.BREADTH_FIRST, StopEvaluator.DEPTH_ONE, ReturnableEvaluator.ALL, DependencyScopeRelations.compile, Direction.BOTH, DependencyScopeRelations.provided, Direction.BOTH);
    }

    /**
     * Proxy forward test
     */
    @Test
    public void traverseType3Test() {
        decorator.traverse(Traverser.Order.BREADTH_FIRST, StopEvaluator.DEPTH_ONE, ReturnableEvaluator.ALL, DependencyScopeRelations.compile, DependencyScopeRelations.runtime);
        verify(node).traverse(Traverser.Order.BREADTH_FIRST, StopEvaluator.DEPTH_ONE, ReturnableEvaluator.ALL, DependencyScopeRelations.compile, DependencyScopeRelations.runtime);
    }

    /**
     * Proxy forward test
     */
    @Test
    public void getGraphDatabaseTest() {
        decorator.getGraphDatabase();
        verify(node).getGraphDatabase();
    }

    /**
     * Proxy forward test
     */
    @Test
    public void hasPropertyTest() {
        decorator.hasProperty("SomeKey");
        verify(node).hasProperty("SomeKey");
    }

    /**
     * Proxy forward test
     */
    @Test
    public void getPropertyKeyTest() {
        decorator.getProperty("SomeKey");
        verify(node).getProperty("SomeKey");
    }

    /**
     * Proxy forward test
     */
    @Test
    public void getPropertyKeyDefaultTest() {
        decorator.getProperty("SomeKey", "DefaultValue");
        verify(node).getProperty("SomeKey", "DefaultValue");
    }

    /**
     * Proxy forward test
     */
    @Test
    public void setPropertyTest() {
        decorator.setProperty("SomeKey", "SomeValue");
        verify(node).setProperty("SomeKey", "SomeValue");
    }

    /**
     * Proxy forward test
     */
    @Test
    public void removePropertyTest() {
        decorator.removeProperty("SomeKey");
        verify(node).removeProperty("SomeKey");
    }

    /**
     * Proxy forward test
     */
    @Test
    public void getPropertyKeysTest() {
        decorator.getPropertyKeys();
        verify(node).getPropertyKeys();
    }

    /**
     * Proxy forward test
     */
    @Test
    @SuppressWarnings("deprecation")
    public void getPropertyValuesTest() {
        decorator.getPropertyValues();
        verify(node).getPropertyValues();
    }


    @Before
    public void setUp() throws Exception {
        node = mock(Node.class);
        decorator = new AbstractNodeDecoratorImpl(node);
    }

}
