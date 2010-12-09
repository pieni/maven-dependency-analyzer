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

import java.io.IOException;
import java.io.StringWriter;

import static junit.framework.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: pieter
 * Date: 9-12-10
 * Time: 21:16
 * To change this template use File | Settings | File Templates.
 */
public class DependencyReportTest {


    @Test
    public void stringWriterTest() throws IOException {

        DependencyReport report = new DependencyReportImpl("reportGID:reportAID");
        report.addVersion("1.0");
        report.addScopedRelation(DependencyScopeRelations.compile, "compileGID:compileAID");
        report.addVersionRelation("1.0", "versionGID:versionAID");
        StringWriter writer = new StringWriter();
        report.createReport(writer);
        System.out.println("writer.toString() = " + writer.toString());
    }

    @Test
    public void logWriterTest() throws IOException {

        DependencyReport report = new DependencyReportImpl("reportGID:reportAID");
        report.addVersion("1.0");
        report.addScopedRelation(DependencyScopeRelations.compile, "compileGID:compileAID");
        report.addVersionRelation("1.0", "versionGID:versionAID");
        LogWriter writer = new LogWriter(new SystemStreamLog());
        report.createReport(writer);
        System.out.println("writer.toString() = " + writer.toString());
    }

    @Test
    public void reportingArtifactName() {
        String reportArtifact = "reportGID:reportAID";
        DependencyReport report = new DependencyReportImpl(reportArtifact);
        assertEquals(reportArtifact, report.getArtifact());
    }

}
