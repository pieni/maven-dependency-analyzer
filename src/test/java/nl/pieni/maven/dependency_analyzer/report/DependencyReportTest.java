/*
 * Copyright  2010 Pieter van der Meer (pieter@pieni.nl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.pieni.maven.dependency_analyzer.report;

import nl.pieni.maven.dependency_analyzer.neo4j.enums.DependencyScopeRelations;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.junit.Test;

import java.awt.*;
import java.io.IOException;
import java.io.StringWriter;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.matches;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Test case for {@link DependencyReport}
 */
public class DependencyReportTest {

    private static final String LINESEPERATOR = System.getProperty("line.separator");

    /**
     * Test the output of the writer
     *
     * @throws IOException
     */
    @Test
    public void stringWriterTest() throws IOException {

        DependencyReport report = new DependencyReportImpl("reportGID:reportAID");
        report.addVersion("1.0");
        report.addScopedRelation(DependencyScopeRelations.compile, "compileGID:compileAID");
        report.addVersionRelation("1.0", "versionGID:versionAID");
        StringWriter writer = mock(StringWriter.class);
        report.createReport(writer);
        verify(writer).write("Report for Artifact: \"reportGID:reportAID\"" + LINESEPERATOR);
        verify(writer).write("Available versions" + LINESEPERATOR);
        verify(writer, times(2)).write("\t1.0" + LINESEPERATOR);
        verify(writer).write("Incoming relations" + LINESEPERATOR);
        verify(writer).write("\tScope: compile" + LINESEPERATOR);
        verify(writer).write("\t\tcompileGID:compileAID" + LINESEPERATOR);
        verify(writer).write("Version specific relations" + LINESEPERATOR);
        verify(writer).write("\t\t" + "versionGID:versionAID" + LINESEPERATOR);
    }


    /**
     * The the writer with a {@link org.apache.maven.plugin.logging.Log} as writer
     *
     * @throws IOException
     */
    @Test
    public void logWriterTest() throws IOException {

        DependencyReport report = new DependencyReportImpl("reportGID:reportAID");
        report.addVersion("1.0");
        report.addScopedRelation(DependencyScopeRelations.compile, "compileGID:compileAID");
        report.addVersionRelation("1.0", "versionGID:versionAID");
        SystemStreamLog systemStreamLog = mock(SystemStreamLog.class);
        LogWriter writer = new LogWriter(systemStreamLog);
        report.createReport(writer);
        verify(systemStreamLog).info("Report for Artifact: \"reportGID:reportAID\"");
        verify(systemStreamLog).info("Available versions");
        verify(systemStreamLog, times(2)).info("\t1.0");
        verify(systemStreamLog).info("Incoming relations");
        verify(systemStreamLog).info("\tScope: compile");
        verify(systemStreamLog).info("\t\tcompileGID:compileAID");
        verify(systemStreamLog).info("Version specific relations");
        verify(systemStreamLog).info("\t\t" + "versionGID:versionAID");
    }

    /**
     * Retrieval of the artifact reported
     */
    @Test
    public void reportingArtifactName() {
        String reportArtifact = "reportGID:reportAID";
        DependencyReport report = new DependencyReportImpl(reportArtifact);
        assertEquals(reportArtifact, report.getArtifact());
    }

}
