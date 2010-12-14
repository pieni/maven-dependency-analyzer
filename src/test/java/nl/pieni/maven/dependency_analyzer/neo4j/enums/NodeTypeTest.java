package nl.pieni.maven.dependency_analyzer.neo4j.enums;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Node type test
 */
public class NodeTypeTest {

    /**
     * Test enum values
     */
    @Test
    public void versionsDependencyTest() {
        NodeType[] values = NodeType.values();
        for (NodeType type : values) {
            NodeType value = NodeType.valueOf(type.toString());
            assertEquals(value, type);
        }
    }

    /**
     * Test from string conversions
     */
    @Test
    public void fromStringTest() {
        NodeType[] values = NodeType.values();
        for (NodeType type : values) {
            NodeType value = NodeType.fromString(type.toString());
            assertEquals(value, type);
        }
    }
}
