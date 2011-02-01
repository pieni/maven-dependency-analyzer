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

import nl.pieni.maven.dependency_analyzer.test_helpers.SimpleLogger;
import org.apache.maven.plugin.logging.Log;
import org.junit.Test;

import java.io.Writer;

import static junit.framework.Assert.assertEquals;
import static org.junit.internal.matchers.StringContains.containsString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.contains;
import static org.mockito.Matchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Created by IntelliJ IDEA.
 * User: pieter
 * Date: 1-2-11
 * Time: 19:19
 * To change this template use File | Settings | File Templates.
 */
public class AbstractDotWriterTest {

    private class TestDotWriter extends AbstractDotWriter {

        protected TestDotWriter(Writer writer, Log LOG) {
            super(writer, LOG);
        }
    }

    @Test
    public void testStartGraph() throws Exception {
        Writer writer = mock(Writer.class);
        TestDotWriter dotWriter = new TestDotWriter(writer, new SimpleLogger());
        dotWriter.startGraph();
        verify(writer).write(startsWith("digraph G {"));
    }

    @Test
    public void testEndGraph() throws Exception {
        Writer writer = mock(Writer.class);
        TestDotWriter dotWriter = new TestDotWriter(writer, new SimpleLogger());
        dotWriter.endGraph();
        verify(writer).write(startsWith("}"));
    }

    @Test
    public void testClose() throws Exception {
        Writer writer = mock(Writer.class);
        TestDotWriter dotWriter = new TestDotWriter(writer, new SimpleLogger());
        dotWriter.close();
        verify(writer).flush();
        verify(writer).close();
    }

    @Test
    public void testGetWriter() throws Exception {
        Writer writer = mock(Writer.class);
        TestDotWriter dotWriter = new TestDotWriter(writer, new SimpleLogger());
        assertEquals(writer, dotWriter.getWriter());
    }

    @Test
    public void testGetLog() throws Exception {
        Writer writer = mock(Writer.class);
        Log log = mock(Log.class);
        TestDotWriter dotWriter = new TestDotWriter(writer, log);
        assertEquals(log, dotWriter.getLog());
    }
}
