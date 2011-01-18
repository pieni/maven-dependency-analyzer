package nl.pieni.maven.dependency_analyzer.neo4j.enums;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Artifact Relations tests
 */
public class ArtifactRelationsTest2 {

    /**
     * Verify all defined relations
     */
    @Test
    public void versionsDependencyTest() {
        ArtifactRelations[] values = ArtifactRelations.values();
        for (ArtifactRelations artifactRelations : values) {
            ArtifactRelations value = ArtifactRelations.valueOf(artifactRelations.toString());
            assertEquals(value, artifactRelations);
        }
    }

}
