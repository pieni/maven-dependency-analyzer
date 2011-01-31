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

package nl.pieni.maven.dependency_analyzer.neo4j.export.dot.writer;

import nl.pieni.maven.dependency_analyzer.neo4j.export.dot.writer.shape.*;
import org.apache.maven.plugin.logging.Log;

import java.io.IOException;
import java.io.Writer;

/**
 * Abstract base implementation for Dot file writers
 * Implements the start and end of a Dot file
 */
public class AbstractDotWriter implements DotWriter {

    private final Writer writer;
    protected final String LINE_SEPARATOR = System.getProperty("line.separator");
    private final Log LOG;

    protected AbstractDotWriter(Writer writer, Log LOG) {
        this.writer = writer;
        this.LOG = LOG;
    }

    /**
     * @inheritDoc
     */
    public void startGraph() throws IOException {
        writer.write(" digraph G {" + LINE_SEPARATOR);
    }

    /**
     * @inheritDoc
     */
    public void endGraph() throws IOException {
        writer.write("}");
    }

    /**
     * @inheritDoc
     */
    public void close() throws IOException {
        writer.flush();
        writer.close();
    }

    /**
     * @inheritDoc
     */
    public Writer getWriter() {
        return this.writer;
    }

    /**
     * @inheritDoc
     */
    public Log getLog() {
        return this.LOG;
    }
}
