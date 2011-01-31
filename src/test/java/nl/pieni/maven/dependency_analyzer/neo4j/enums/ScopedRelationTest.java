package nl.pieni.maven.dependency_analyzer.neo4j.enums;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Dependency scope tests
 */
public class ScopedRelationTest {

    /**
     * Test all enum values
     */
    @Test
    public void versionsDependencyTest() {
        ScopedRelation[] values = ScopedRelation.values();
        for (ScopedRelation scopedRelation : values) {
            ScopedRelation value = ScopedRelation.valueOf(scopedRelation.toString());
            assertEquals(value, scopedRelation);
        }
    }

    /**
     * Test the from string conversion
     */
    @Test
    public void fromStringTest() {
        ScopedRelation[] values = ScopedRelation.values();
        for (ScopedRelation scopedRelation : values) {
            ScopedRelation value = ScopedRelation.fromString(scopedRelation.toString());
            assertEquals(value, scopedRelation);
        }
    }
}
