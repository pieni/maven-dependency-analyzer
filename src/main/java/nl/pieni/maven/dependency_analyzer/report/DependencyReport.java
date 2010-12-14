package nl.pieni.maven.dependency_analyzer.report;

import nl.pieni.maven.dependency_analyzer.database.DependencyDatabaseSearcher;
import nl.pieni.maven.dependency_analyzer.enums.DependencyScopeRelations;
import nl.pieni.maven.dependency_analyzer.node.ArtifactNode;

import java.io.IOException;
import java.io.Writer;

/**
 * Place holder for the data collected for the report.
 */
public interface DependencyReport {

    /**
     * Print the report using the provided writer
     *
     * @param writer the Writer
     * @throws IOException In case of error
     */
    void createReport(ArtifactNode artifactNode, Writer writer) throws IOException;

}
