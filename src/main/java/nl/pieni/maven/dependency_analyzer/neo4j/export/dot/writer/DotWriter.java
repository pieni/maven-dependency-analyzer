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

import org.apache.maven.plugin.logging.Log;

import java.io.IOException;
import java.io.Writer;

/**
 * Interface for all dot writers
 */
public interface DotWriter {

    /**
     * Insert the standard start of a DOT graph
     *
     * @throws java.io.IOException in case of error
     */
    void startGraph() throws IOException;
    /**
     * End the graph correctly
     *
     * @throws IOException in case of error;
     */
    void endGraph() throws IOException;

    /**
     * Retrieve the writer object
     * @return the Writer
     */
    Writer getWriter();

    /**
     * Close the file (write the Dot ending)
     * @throws IOException
     */
    void close() throws IOException;

    /**
     * Retrieve the logger
     * @return log
     */
    Log getLog();
}
