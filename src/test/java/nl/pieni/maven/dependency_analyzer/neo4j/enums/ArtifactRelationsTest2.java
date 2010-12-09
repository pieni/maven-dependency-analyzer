package nl.pieni.maven.dependency_analyzer.neo4j.enums;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: pieter
 * Date: 9-12-10
 * Time: 22:57
 * To change this template use File | Settings | File Templates.
 */
public class ArtifactRelationsTest2 {

    @Test
    public void versionsDependencyTest() {
        ArtifactRelations[] values = ArtifactRelations.values();
        for (ArtifactRelations artifactRelations : values) {
            ArtifactRelations value = ArtifactRelations.valueOf(artifactRelations.toString());
            assertEquals(value, artifactRelations);
        }
    }

}
