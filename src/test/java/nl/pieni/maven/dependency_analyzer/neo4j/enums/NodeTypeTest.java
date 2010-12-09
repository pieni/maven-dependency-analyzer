package nl.pieni.maven.dependency_analyzer.neo4j.enums;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: pieter
 * Date: 9-12-10
 * Time: 22:56
 * To change this template use File | Settings | File Templates.
 */
public class NodeTypeTest {

    @Test
    public void versionsDependencyTest() {
        NodeType[] values = NodeType.values();
        for (NodeType type : values) {
            NodeType value = NodeType.valueOf(type.toString());
            assertEquals(value, type);
        }
    }

    @Test
    public void fromStringTest() {
        NodeType[] values = NodeType.values();
        for (NodeType type : values) {
            NodeType value = NodeType.fromString(type.toString());
            assertEquals(value, type);
        }
    }
}
