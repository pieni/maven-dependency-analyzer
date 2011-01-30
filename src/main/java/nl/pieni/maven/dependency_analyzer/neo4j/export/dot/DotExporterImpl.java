/*
 * Copyright (c) 2011 Pieter van der Meer (pieter@pieni.nl)
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

package nl.pieni.maven.dependency_analyzer.neo4j.export.dot;

import nl.pieni.maven.dependency_analyzer.database.DependencyDatabase;
import nl.pieni.maven.dependency_analyzer.dot.DotExporter;
import nl.pieni.maven.dependency_analyzer.dot.NodeWriter;
import nl.pieni.maven.dependency_analyzer.neo4j.enums.ArtifactRelations;
import nl.pieni.maven.dependency_analyzer.neo4j.enums.NodeProperties;
import nl.pieni.maven.dependency_analyzer.neo4j.enums.NodeType;
import nl.pieni.maven.dependency_analyzer.neo4j.export.dot.convert.RawExport;
import nl.pieni.maven.dependency_analyzer.neo4j.export.dot.shapes.DotEdge;
import nl.pieni.maven.dependency_analyzer.neo4j.export.dot.shapes.DotShape;
import nl.pieni.maven.dependency_analyzer.neo4j.node.ArtifactNodeDecorator;
import nl.pieni.maven.dependency_analyzer.neo4j.node.GroupNodeDecorator;
import nl.pieni.maven.dependency_analyzer.neo4j.node.VersionNodeDecorator;
import org.apache.maven.plugin.logging.Log;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Export of the graph (partial) to a file in the dot language. see http://graphviz.org
 */
public class DotExporterImpl implements DotExporter {

    private final DependencyDatabase<GraphDatabaseService, Node> dependencyDatabase;

    private final Log LOG;
    private NodeWriter nodeWriter;
    private Map<Node, Set<Relationship>> exportNodeMap = new HashMap<Node, Set<Relationship>>();
    private final NodeSelector nodeSelector;

    public DotExporterImpl(DependencyDatabase<GraphDatabaseService, Node> dependencyDatabase, Log log) {
        this.dependencyDatabase = dependencyDatabase;
        this.LOG = log;
        this.nodeSelector = new NodeSelector(dependencyDatabase, log);
    }

    @Override
    public void setIncludePatters(List<String> includeFilterPatterns) {
        this.nodeSelector.setIncludeFilterPatterns(includeFilterPatterns);
    }

    @Override
    public void setIncludeVersions(boolean includeVersions) {
        this.nodeSelector.setIncludeVersions(includeVersions);
    }

    @Override
    public void export(NodeWriter nodeWriter) throws IOException {
        exportNodeMap = nodeSelector.selectNodesAndRelations();

        exportRaw(nodeWriter);
    }

    private void exportRaw(NodeWriter writer) throws IOException {

        exportNodeMap = nodeSelector.selectNodesAndRelations();

        RawExport exporter = new RawExport(writer, exportNodeMap);

        exporter.writeDotFile();
    }
}
