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

package nl.pieni.maven.dependency_analyzer.neo4j.export.dot.writer.shape;

import nl.pieni.maven.dependency_analyzer.neo4j.export.dot.shapes.AbstractDotShape;
import nl.pieni.maven.dependency_analyzer.neo4j.export.dot.shapes.DotEdge;
import nl.pieni.maven.dependency_analyzer.neo4j.export.dot.writer.AbstractDotWriter;
import org.apache.maven.plugin.logging.Log;

import java.io.IOException;
import java.io.Writer;
import java.util.Set;

/**
 * Implementation of a Dot Writer
 */
public class ShapeDotWriter extends AbstractDotWriter {

    /**
     * Default constructor
     *
     * @param writer the Writer
     * @param LOG    the logger
     */
    public ShapeDotWriter(Writer writer, Log LOG) {
        super(writer, LOG);
    }

    /**
     * Write the shapes and edges
     *
     * @param shapeAbstracts the shapes
     * @param edges the edges
     * @throws IOException in case of error
     */
    public void write(Set<AbstractDotShape> shapeAbstracts, Set<DotEdge> edges) throws IOException {

        startGraph();

        int shapeCount = 0;
        for (AbstractDotShape shapeAbstract : shapeAbstracts) {
            if (getLog().isDebugEnabled()) {
                getLog().debug("Writing: " + shapeAbstract.toString());
            }
            getWriter().write(shapeAbstract.toString() + LINE_SEPARATOR);
            shapeCount++;
        }

        int edgeCount = 0;
        for (DotEdge edge : edges) {
            if (getLog().isDebugEnabled()) {
                getLog().debug("Writing: " + edge.toString());
            }
            getWriter().write(edge.toString() + LINE_SEPARATOR);
            edgeCount++;
        }

        endGraph();

        super.close();

        getLog().info("Exported " + shapeCount + " shapeAbstracts");
        getLog().info("Exported " + edgeCount + " edges");
    }


}
