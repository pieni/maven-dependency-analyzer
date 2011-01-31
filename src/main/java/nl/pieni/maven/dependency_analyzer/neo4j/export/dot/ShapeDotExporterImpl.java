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
import nl.pieni.maven.dependency_analyzer.neo4j.export.dot.convert.Neo4J2Dot;
import nl.pieni.maven.dependency_analyzer.neo4j.export.dot.writer.shape.ShapeDotWriter;
import org.apache.maven.plugin.logging.Log;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

import java.io.IOException;

/**
 * Implementation of the shape exporter
 */
public class ShapeDotExporterImpl extends AbstractDotExporter<ShapeDotWriter> {

    /**
     * Default constructor
     * @param dependencyDatabase the DB
     * @param log the Logger
     */
    public ShapeDotExporterImpl(DependencyDatabase<GraphDatabaseService, Node> dependencyDatabase, Log log) {
        super(dependencyDatabase, log);
    }

    /**
     * @InheritDoc
     */
    @Override
    public void export(ShapeDotWriter writer) throws IOException {
        Neo4J2Dot neo4J2Dot = new Neo4J2Dot(getNodesAndRelations());
        writer.write(neo4J2Dot.getShapes(), neo4J2Dot.getEdges());
    }
}
