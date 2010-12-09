package nl.pieni.maven.dependency_analyzer.neo4j.enums;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: pieter
 * Date: 9-12-10
 * Time: 22:47
 * To change this template use File | Settings | File Templates.
 */
public class DependencyScopeRelationsTest {
    @Test
    public void versionsDependencyTest() {
        DependencyScopeRelations[] values = DependencyScopeRelations.values();
        for (DependencyScopeRelations dependencyScopeRelations : values) {
            DependencyScopeRelations value = DependencyScopeRelations.valueOf(dependencyScopeRelations.toString());
            assertEquals(value, dependencyScopeRelations);
        }
    }

    @Test
    public void fromStringTest() {
        DependencyScopeRelations[] values = DependencyScopeRelations.values();
        for (DependencyScopeRelations dependencyScopeRelations : values) {
            DependencyScopeRelations value = DependencyScopeRelations.fromString(dependencyScopeRelations.toString());
            assertEquals(value, dependencyScopeRelations);
        }
    }
}
