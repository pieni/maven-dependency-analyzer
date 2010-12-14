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

import nl.pieni.maven.dependency_analyzer.database.DependencyDatabaseSearcher;
import nl.pieni.maven.dependency_analyzer.enums.DependencyScopeRelations;
import nl.pieni.maven.dependency_analyzer.node.ArtifactNode;
import nl.pieni.maven.dependency_analyzer.node.GroupNode;
import nl.pieni.maven.dependency_analyzer.node.VersionNode;
import nl.pieni.maven.dependency_analyzer.report.impl.DependencyReportImpl;
import nl.pieni.maven.dependency_analyzer.report.log.LogWriter;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test case for {@link DependencyReport}
 */
public class DependencyReportTest {

    private static final String LINESEPERATOR = System.getProperty("line.separator");

    /*
     Create a set of artifact nodes and parent, mock the searcher and return these

                reportGroupNode                     otherGroupNode
                        |                               |
                reportArtifactNode -> compile ->    otherArtifactNode
                   |       |                            |
        reportVersion1(1.0)  reportVersion2(2.0)              otherVersionNode(1.0)
                |          |                            |
                ------<-------------------------<-------
    */
    private GroupNode reportGroupNode;
    private ArtifactNode reportArtifactNode;
    private VersionNode reportVersion1;
    private VersionNode reportVersion2;
    private GroupNode otherGroupNode;
    private ArtifactNode otherArtifactNode;
    private VersionNode otherVersionNode;
    private DependencyDatabaseSearcher searcher;

    private void setup() {
        reportArtifactNode = mock(ArtifactNode.class);
        when(reportArtifactNode.getArtifactId()).thenReturn("reportAID");
        when(reportArtifactNode.getType()).thenReturn("jar");
        reportGroupNode = mock(GroupNode.class);
        when(reportGroupNode.getGroupId()).thenReturn("reportGID");
        reportVersion1 = mock(VersionNode.class);
        when(reportVersion1.getVersion()).thenReturn("1.0");
        reportVersion2 = mock(VersionNode.class);
        when(reportVersion2.getVersion()).thenReturn("2.0");

        //The parent child relations
        when(reportArtifactNode.getParent()).thenReturn(reportGroupNode);
        when(reportVersion1.getParent()).thenReturn(reportArtifactNode);
        when(reportVersion2.getParent()).thenReturn(reportArtifactNode);

        otherGroupNode = mock(GroupNode.class);
        when(otherGroupNode.getGroupId()).thenReturn("otherGID");
        otherArtifactNode = mock(ArtifactNode.class);
        when(otherArtifactNode.getArtifactId()).thenReturn("otherAID");
        when(otherArtifactNode.getType()).thenReturn("jar");
        otherVersionNode = mock(VersionNode.class);
        when(otherVersionNode.getVersion()).thenReturn("1.0");
        when(otherVersionNode.getParent()).thenReturn(otherArtifactNode);
        when(otherArtifactNode.getParent()).thenReturn(otherGroupNode);

        searcher = mock(DependencyDatabaseSearcher.class);

        List<VersionNode> versionNodes = new ArrayList<VersionNode>();
        versionNodes.add(reportVersion1);
        versionNodes.add(reportVersion2);
        when(searcher.getVersionNodes(reportArtifactNode)).thenReturn(versionNodes);

        Map<DependencyScopeRelations, List<ArtifactNode>> dependingArtifacts = new HashMap<DependencyScopeRelations, List<ArtifactNode>>();
        List<ArtifactNode> artifacts = new ArrayList<ArtifactNode>();
        artifacts.add(otherArtifactNode);
        dependingArtifacts.put(DependencyScopeRelations.compile, artifacts);
        when(searcher.getDependingArtifacts(reportArtifactNode)).thenReturn(dependingArtifacts);


        Map<VersionNode, List<VersionNode>> versionDependencyMap = new HashMap<VersionNode, List<VersionNode>>();
        List<VersionNode> version1DependencyList = new ArrayList<VersionNode>();
        version1DependencyList.add(otherVersionNode);
        versionDependencyMap.put(reportVersion1, version1DependencyList);
        versionDependencyMap.put(reportVersion2, version1DependencyList);

        when(searcher.getVersionDependencies(reportArtifactNode)).thenReturn(versionDependencyMap);
    }

    /**
     * Test the output of the writer
     *
     * @throws IOException When IO Error
     */
    @Test
    public void stringWriterTest() throws IOException {

        setup();
        DependencyReport report = new DependencyReportImpl(searcher);
        StringWriter writer = mock(StringWriter.class);
        report.createReport(reportArtifactNode, writer);

        verify(writer).write("Report for Artifact: \"reportGID:reportAID\"" + LINESEPERATOR);
        verify(writer).write("Available versions" + LINESEPERATOR);
        verify(writer, times(2)).write("\t1.0" + LINESEPERATOR);
        verify(writer, times(2)).write("\t2.0" + LINESEPERATOR);
        verify(writer).write("Incoming relations" + LINESEPERATOR);
        verify(writer).write("\tScope: compile" + LINESEPERATOR);
        verify(writer, times(2)).write("\t\totherGID:otherAID:jar:1.0" + LINESEPERATOR);
        verify(writer).write("\t\totherGID:otherAID:jar" + LINESEPERATOR);
        verify(writer).write("Version specific relations" + LINESEPERATOR);

    }


    /**
     * The the writer with a {@link org.apache.maven.plugin.logging.Log} as writer
     *
     * @throws IOException When IO Error
     */
    @Test
    public void logWriterTest() throws IOException {

        setup();
        DependencyReport report = new DependencyReportImpl(searcher);

        SystemStreamLog systemStreamLog = mock(SystemStreamLog.class);
        LogWriter writer = new LogWriter(systemStreamLog);
        report.createReport(reportArtifactNode, writer);

        verify(systemStreamLog).info("Report for Artifact: \"reportGID:reportAID\"");
        verify(systemStreamLog).info("Available versions" );
        verify(systemStreamLog, times(2)).info("\t1.0" );
        verify(systemStreamLog, times(2)).info("\t2.0" );
        verify(systemStreamLog).info("Incoming relations" );
        verify(systemStreamLog).info("\tScope: compile");
        verify(systemStreamLog, times(2)).info("\t\totherGID:otherAID:jar:1.0");
        verify(systemStreamLog).info("\t\totherGID:otherAID:jar");
        verify(systemStreamLog).info("Version specific relations");
    }

}
