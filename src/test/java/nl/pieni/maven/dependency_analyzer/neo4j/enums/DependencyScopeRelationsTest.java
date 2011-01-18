package nl.pieni.maven.dependency_analyzer.neo4j.enums;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Dependency scope tests
 */
public class DependencyScopeRelationsTest {

    /**
     * Test all enum values
     */
    @Test
    public void versionsDependencyTest() {
        DependencyScopeRelations[] values = DependencyScopeRelations.values();
        for (DependencyScopeRelations dependencyScopeRelations : values) {
            DependencyScopeRelations value = DependencyScopeRelations.valueOf(dependencyScopeRelations.toString());
            assertEquals(value, dependencyScopeRelations);
        }
    }

    /**
     * Test the from string conversion
     */
    @Test
    public void fromStringTest() {
        DependencyScopeRelations[] values = DependencyScopeRelations.values();
        for (DependencyScopeRelations dependencyScopeRelations : values) {
            DependencyScopeRelations value = DependencyScopeRelations.fromString(dependencyScopeRelations.toString());
            assertEquals(value, dependencyScopeRelations);
        }
    }
}
