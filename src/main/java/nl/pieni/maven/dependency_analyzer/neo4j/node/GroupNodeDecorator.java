package nl.pieni.maven.dependency_analyzer.neo4j.node;

import nl.pieni.maven.dependency_analyzer.neo4j.enums.NodeProperties;
import nl.pieni.maven.dependency_analyzer.neo4j.enums.NodeType;
import nl.pieni.maven.dependency_analyzer.node.GroupNode;
import org.apache.maven.model.Dependency;
import org.neo4j.graphdb.Node;

/**
 * Created by IntelliJ IDEA.
 * User: pieter
 * Date: 11-12-10
 * Time: 19:48
 * To change this template use File | Settings | File Templates.
 */
public class GroupNodeDecorator extends AbstractNodeDecorator implements GroupNode {

    public GroupNodeDecorator(Node node, Dependency dependency) {
        super(node);
        setProperty(NodeProperties.NODE_TYPE, NodeType.GroupNode);
        setDependency(dependency);
    }

    public GroupNodeDecorator(Node node) {
        super(node);
        if (node.getProperty(NodeProperties.NODE_TYPE) != NodeType.GroupNode) {
            throw new IllegalArgumentException("node " + node.getId() + " is not a " + NodeType.GroupNode);
        }
    }


    @Override
    public void setDependency(Dependency dependency) {
        setProperty(NodeProperties.GROUP_ID, dependency.getGroupId());
    }

    @Override
    public String getGroupId() {
        return (String)super.getProperty(NodeProperties.GROUP_ID);
    }
}
