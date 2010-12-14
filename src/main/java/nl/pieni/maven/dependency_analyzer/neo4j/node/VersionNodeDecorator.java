package nl.pieni.maven.dependency_analyzer.neo4j.node;

import nl.pieni.maven.dependency_analyzer.enums.ArtifactRelations;
import nl.pieni.maven.dependency_analyzer.neo4j.enums.NodeProperties;
import nl.pieni.maven.dependency_analyzer.neo4j.enums.NodeType;
import nl.pieni.maven.dependency_analyzer.node.ArtifactNode;
import nl.pieni.maven.dependency_analyzer.node.VersionNode;
import org.apache.maven.model.Dependency;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

/**
 * Created by IntelliJ IDEA.
 * User: pieter
 * Date: 11-12-10
 * Time: 10:13
 * To change this template use File | Settings | File Templates.
 */
public class VersionNodeDecorator extends AbstractNodeDecorator implements VersionNode {


    public VersionNodeDecorator(Node node, Dependency dependency) {
        super(node);
        setProperty(NodeProperties.NODE_TYPE, NodeType.VersionNode);
        setDependency(dependency);
    }

    public VersionNodeDecorator(Node node) {
        super(node);
        if (node.getProperty(NodeProperties.NODE_TYPE) != NodeType.VersionNode) {
            throw new IllegalArgumentException("node " + node.getId() + " is not a " + NodeType.VersionNode);
        }
    }

    @Override
    public void setDependency(Dependency dependency) {
        setProperty(NodeProperties.VERSION, dependency.getVersion());
    }

    public String getVersion() {
        return (String) getProperty(NodeProperties.VERSION);
    }

    @Override
    public ArtifactNode getParent() {
        Iterable<Relationship> parentRelations = getRelationships(ArtifactRelations.version, Direction.INCOMING);
        for (Relationship parentRelation : parentRelations) {
            Node parent = parentRelation.getOtherNode(this);
            return new ArtifactNodeDecorator(parent);
        }

        throw new IllegalArgumentException("Database inconsistent" + this.toString() + " has no parent");
    }

}
