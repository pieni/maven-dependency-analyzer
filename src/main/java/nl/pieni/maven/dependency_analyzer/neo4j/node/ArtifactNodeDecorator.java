package nl.pieni.maven.dependency_analyzer.neo4j.node;

import nl.pieni.maven.dependency_analyzer.enums.ArtifactRelations;
import nl.pieni.maven.dependency_analyzer.neo4j.enums.NodeProperties;
import nl.pieni.maven.dependency_analyzer.neo4j.enums.NodeType;
import nl.pieni.maven.dependency_analyzer.node.ArtifactNode;
import nl.pieni.maven.dependency_analyzer.node.GroupNode;
import org.apache.maven.model.Dependency;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

/**
 * Created by IntelliJ IDEA.
 * User: pieter
 * Date: 11-12-10
 * Time: 19:44
 * To change this template use File | Settings | File Templates.
 */
public class ArtifactNodeDecorator extends AbstractNodeDecorator implements ArtifactNode {

    public ArtifactNodeDecorator(Node node, Dependency dependency) {
        super(node);
        setProperty(NodeProperties.NODE_TYPE, NodeType.ArtifactNode);
        setDependency(dependency);
    }

    public ArtifactNodeDecorator(Node node) {
        super(node);
        if (node.getProperty(NodeProperties.NODE_TYPE) != NodeType.ArtifactNode) {
            throw new IllegalArgumentException("node " + node.getId() + " is not a " + NodeType.ArtifactNode);
        }
    }

    @Override
    public void setDependency(Dependency dependency) {
        setProperty(NodeProperties.ARTIFACT_ID, dependency.getArtifactId());
        setProperty(NodeProperties.TYPE, dependency.getType());
    }

    @Override
    public String getArtifactId() {
        return (String) getProperty(NodeProperties.ARTIFACT_ID);
    }

    @Override
    public String getType() {
        return (String) getProperty(NodeProperties.TYPE);
    }

    @Override
    public GroupNode getParent() {
        Iterable<Relationship> hasRelations = getRelationships(ArtifactRelations.has, Direction.INCOMING);
        for (Relationship relationship : hasRelations) {
            Node relationNode = relationship.getOtherNode(this);
            return new GroupNodeDecorator(relationNode);
        }

        throw new IllegalArgumentException("Database inconsistent" + this.toString() + " has no parent");
    }


}
