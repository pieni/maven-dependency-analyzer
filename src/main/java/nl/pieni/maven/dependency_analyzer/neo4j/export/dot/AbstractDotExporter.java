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
import nl.pieni.maven.dependency_analyzer.neo4j.export.dot.writer.raw.NodeWriter;
import org.apache.maven.plugin.logging.Log;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Base class for Dot exports
 */
public abstract class AbstractDotExporter<T> implements DotExporter<T> {

    private final NodeSelector nodeSelector;

    AbstractDotExporter(DependencyDatabase<GraphDatabaseService, Node> dependencyDatabase, Log log) {
        DependencyDatabase<GraphDatabaseService, Node> dependencyDatabase1=dependencyDatabase;
        Log LOG=log;
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

    Map<Node, Set<Relationship>> getNodesAndRelations() {
        return nodeSelector.selectNodesAndRelations();
    }

    @Override
    public abstract void export(T writer) throws IOException;

}
