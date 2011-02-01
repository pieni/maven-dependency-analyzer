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

import nl.pieni.maven.dependency_analyzer.neo4j.export.dot.shapes.ArtifactDotShape;
import nl.pieni.maven.dependency_analyzer.neo4j.export.dot.shapes.DotEdge;
import nl.pieni.maven.dependency_analyzer.test_helpers.SimpleLogger;
import org.junit.Test;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import static org.junit.internal.matchers.StringContains.containsString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test class for Shape Dot Writer
 * TODO Implement this
 */
public class ShapeDotWriterTest {
    @Test
    public void testSimple() throws IOException {
        Writer writer = mock(Writer.class);
        ShapeDotWriter dotWriter = new ShapeDotWriter(writer, new SimpleLogger());
        Set<ArtifactDotShape> shapes = new HashSet<ArtifactDotShape>();
        ArtifactDotShape artifactDotShape = mock(ArtifactDotShape.class);
        when(artifactDotShape.toString()).thenReturn("artifact to String");
        shapes.add(artifactDotShape);
        Set<DotEdge> edges = new HashSet<DotEdge>();
        DotEdge dotEdge = mock(DotEdge.class);
        when(dotEdge.toString()).thenReturn("dotedge to String");
        edges.add(dotEdge);

        dotWriter.write(shapes, edges);
        verify(writer).write(argThat(containsString("digraph G {")));
        verify(writer).write(argThat(containsString("artifact to String")));
        verify(writer).write(argThat(containsString("dotedge to String")));
        verify(writer).write(argThat(containsString("}")));


    }


}
