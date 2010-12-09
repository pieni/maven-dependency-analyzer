package nl.pieni.maven.dependency_analyzer.report;

import nl.pieni.maven.dependency_analyzer.neo4j.enums.DependencyScopeRelations;

import java.io.IOException;
import java.io.Writer;

/**
 * Place holder for the data collected for the report.
 */
public interface DependencyReport {
    /**
     * Add a available version for this report
     * @param version
     */
    void addVersion(String version);

    /**
     * Add a report item for a dependency with the specified scope
     * @param scope the Scope
     * @param artifact the Artifact
     */
    void addScopedRelation(DependencyScopeRelations scope, String artifact);

    /**
     * Add a report item for a version dependency
     * @param version the Version
     * @param artifact the Artifact
     */
    void addVersionRelation(String version, String artifact);

    /**
     * Print the report using the provided writer
     * @param writer the Writer
     * @throws IOException In case of error
     */
    void createReport(Writer writer) throws IOException;

    /**
     * Return the artifact that the report is on
     * @return the artifact.
     */
    String getArtifact();
}
