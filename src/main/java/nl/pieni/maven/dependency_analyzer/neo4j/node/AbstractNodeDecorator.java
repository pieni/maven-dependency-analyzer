package nl.pieni.maven.dependency_analyzer.neo4j.node;

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
 * Created by IntelliJ IDEA.
 * User: pieter
 * Date: 11-12-10
 * Time: 10:16
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractNodeDecorator implements Node {
    private Node node;

    public AbstractNodeDecorator(Node node) {
        this.node = node;
    }


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

    // Proxy methods from the interface
    @Override
    public long getId() {
        return node.getId();
    }

    @Override
    public void delete() {
        node.delete();
    }

    @Override
    public Iterable<Relationship> getRelationships() {
        return node.getRelationships();
    }

    @Override
    public boolean hasRelationship() {
        return node.hasRelationship();
    }

    @Override
    public Iterable<Relationship> getRelationships(RelationshipType... types) {
        return node.getRelationships(types);
    }

    @Override
    public boolean hasRelationship(RelationshipType... types) {
        return node.hasRelationship(types);
    }

    @Override
    public Iterable<Relationship> getRelationships(Direction dir) {
        return node.getRelationships(dir);
    }

    @Override
    public boolean hasRelationship(Direction dir) {
        return node.hasRelationship(dir);
    }

    @Override
    public Iterable<Relationship> getRelationships(RelationshipType type, Direction dir) {
        return node.getRelationships(type, dir);
    }

    @Override
    public boolean hasRelationship(RelationshipType type, Direction dir) {
        return node.hasRelationship(type, dir);
    }

    @Override
    public Relationship getSingleRelationship(RelationshipType type, Direction dir) {
        return node.getSingleRelationship(type, dir);
    }

    @Override
    public Relationship createRelationshipTo(Node otherNode, RelationshipType type) {
        return node.createRelationshipTo(otherNode, type);
    }

    @Override
    public Traverser traverse(Traverser.Order traversalOrder, StopEvaluator stopEvaluator, ReturnableEvaluator returnableEvaluator, RelationshipType relationshipType, Direction direction) {
        return node.traverse(traversalOrder, stopEvaluator, returnableEvaluator, relationshipType, direction);
    }

    @Override
    public Traverser traverse(Traverser.Order traversalOrder, StopEvaluator stopEvaluator, ReturnableEvaluator returnableEvaluator, RelationshipType firstRelationshipType, Direction firstDirection, RelationshipType secondRelationshipType, Direction secondDirection) {
        return node.traverse(traversalOrder, stopEvaluator, returnableEvaluator, firstRelationshipType, firstDirection, secondRelationshipType, secondDirection);
    }

    @Override
    public Traverser traverse(Traverser.Order traversalOrder, StopEvaluator stopEvaluator, ReturnableEvaluator returnableEvaluator, Object... relationshipTypesAndDirections) {
        return node.traverse(traversalOrder, stopEvaluator, returnableEvaluator, relationshipTypesAndDirections);
    }

    @Override
    public GraphDatabaseService getGraphDatabase() {
        return node.getGraphDatabase();
    }

    @Override
    public boolean hasProperty(String key) {
        return node.hasProperty(key);
    }

    @Override
    public Object getProperty(String key) {
        return node.getProperty(key);
    }

    @Override
    public Object getProperty(String key, Object defaultValue) {
        return node.getProperty(key, defaultValue);
    }

    @Override
    public void setProperty(String key, Object value) {
        node.setProperty(key, value);
    }

    @Override
    public Object removeProperty(String key) {
        return node.removeProperty(key);
    }

    @Override
    public Iterable<String> getPropertyKeys() {
        return node.getPropertyKeys();
    }

    @Override
    @Deprecated
    public Iterable<Object> getPropertyValues() {
        return node.getPropertyValues();

    }
}
