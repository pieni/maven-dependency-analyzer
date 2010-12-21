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

import nl.pieni.maven.dependency_analyzer.node.DependencyNode;
import org.apache.maven.model.Dependency;
import org.jetbrains.annotations.NotNull;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.ReturnableEvaluator;
import org.neo4j.graphdb.StopEvaluator;
import org.neo4j.graphdb.Traverser;

/**
 * Abstract Neo4j Node decorator
 * Mainly code to proxy to the {@link Node} object
 */
public abstract class AbstractNodeDecorator implements Node, DependencyNode {
    private final Node node;

    /**
     * Default constructor
     *
     * @param node the Node to decorate
     */
    public AbstractNodeDecorator(Node node) {
        this.node = node;
    }

    /**
     * Constructor for the default construction
     *
     * @param node       The node
     * @param dependency the dependency
     */
    AbstractNodeDecorator(Node node, Dependency dependency) {
        this.node = node;
        setDependency(dependency);
    }

    /**
     * @inheritDoc
     */
    @Override
    public abstract void setDependency(Dependency dependency);

    /**
     * Create a string represention of the node
     *
     * @return a String
     */
    public String toString() {
        StringBuffer buff = new StringBuffer();
        buff.append("Node{ Id = ");
        buff.append(getId());
        for (String key : getPropertyKeys()) {
            buff.append(" key = ");
            buff.append(key);
            buff.append(" value = ");
            buff.append(getProperty(key));
        }
        buff.append("}");

        return buff.toString();
    }

    /**
     * @InheritDoc
     */
    @Override
    public long getId() {
        return node.getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete() {
        node.delete();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<Relationship> getRelationships() {
        return node.getRelationships();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasRelationship() {
        return node.hasRelationship();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<Relationship> getRelationships(RelationshipType... types) {
        return node.getRelationships(types);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasRelationship(RelationshipType... types) {
        return node.hasRelationship(types);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<Relationship> getRelationships(Direction dir) {
        return node.getRelationships(dir);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasRelationship(Direction dir) {
        return node.hasRelationship(dir);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<Relationship> getRelationships(RelationshipType type, Direction dir) {
        return node.getRelationships(type, dir);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasRelationship(RelationshipType type, Direction dir) {
        return node.hasRelationship(type, dir);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Relationship getSingleRelationship(RelationshipType type, Direction dir) {
        return node.getSingleRelationship(type, dir);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Relationship createRelationshipTo(Node otherNode, RelationshipType type) {
        return node.createRelationshipTo(otherNode, type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Traverser traverse(Traverser.Order traversalOrder, StopEvaluator stopEvaluator, ReturnableEvaluator returnableEvaluator, RelationshipType relationshipType, Direction direction) {
        return node.traverse(traversalOrder, stopEvaluator, returnableEvaluator, relationshipType, direction);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Traverser traverse(Traverser.Order traversalOrder, StopEvaluator stopEvaluator, ReturnableEvaluator returnableEvaluator, RelationshipType firstRelationshipType, Direction firstDirection, RelationshipType secondRelationshipType, Direction secondDirection) {
        return node.traverse(traversalOrder, stopEvaluator, returnableEvaluator, firstRelationshipType, firstDirection, secondRelationshipType, secondDirection);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Traverser traverse(Traverser.Order traversalOrder, StopEvaluator stopEvaluator, ReturnableEvaluator returnableEvaluator, Object... relationshipTypesAndDirections) {
        return node.traverse(traversalOrder, stopEvaluator, returnableEvaluator, relationshipTypesAndDirections);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphDatabaseService getGraphDatabase() {
        return node.getGraphDatabase();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasProperty(String key) {
        return node.hasProperty(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getProperty(String key) {
        return node.getProperty(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getProperty(String key, Object defaultValue) {
        return node.getProperty(key, defaultValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setProperty(String key, Object value) {
        node.setProperty(key, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object removeProperty(String key) {
        return node.removeProperty(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<String> getPropertyKeys() {
        return node.getPropertyKeys();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Deprecated
    public Iterable<Object> getPropertyValues() {
        return node.getPropertyValues();

    }
}
