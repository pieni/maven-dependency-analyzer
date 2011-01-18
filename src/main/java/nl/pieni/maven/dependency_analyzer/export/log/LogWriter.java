/*
 * Copyright (c) 2010 Pieter van der Meer (pieter@pieni.nl)
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
package nl.pieni.maven.dependency_analyzer.export.log;

import org.apache.maven.plugin.logging.Log;

import java.io.IOException;
import java.io.Writer;
import java.util.StringTokenizer;

/**
 * Wrapper arround the {@link Log} implementation of the plugin logger and allow usage as a
 * writer for the {@link nl.pieni.maven.dependency_analyzer.export.DependencyReport}
 */
public class LogWriter extends Writer {
    private static final String LINESEPERATOR = System.getProperty("line.separator");
    private final Log LOGGER;

    /**
     * Default constructor
     *
     * @param logger the Plugin logger
     */
    public LogWriter(Log logger) {
        this.LOGGER = logger;
    }

    /**
     * Fix the toString conversion of the data (sub stringing)
     *
     * @param cbuf the Buffer
     * @param off  the offset
     * @param len  the length
     * @return substring between offset + len
     */
    private String bufferToString(char[] cbuf, int off, int len) {
        String tmp = String.valueOf(cbuf);
        return tmp.substring(off, len);

    }

    /**
     * @inheritDoc
     */
    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        StringTokenizer strTok = new StringTokenizer(bufferToString(cbuf, off, len), LINESEPERATOR);
        while (strTok.hasMoreElements()) {
            LOGGER.info(strTok.nextToken());
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public void flush() throws IOException {
        //Intentionally left blank
    }

    /**
     * @inheritDoc
     */
    @Override
    public void close() throws IOException {
        //Intentionally left blank
    }
}
