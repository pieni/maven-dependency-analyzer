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

package nl.pieni.maven.dependency_analyzer.neo4j.export.dot.shapes;

import nl.pieni.maven.dependency_analyzer.dot.NodeShapes.NodeShape;
import nl.pieni.maven.dependency_analyzer.neo4j.enums.NodeProperties;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.Node;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by IntelliJ IDEA.
 * User: pieter
 * Date: 28-1-11
 * Time: 15:21
 * To change this template use File | Settings | File Templates.
 */
public class ArtifactDotShapeTest {

    private Node node;
    private ArtifactDotShape shape;
    public static final String ARTIFACT_NAME = "theArtifactId";

    @Before
    public void before() {
        node = mock(Node.class);
        when(node.getId()).thenReturn(1L);
        when(node.getProperty(NodeProperties.ARTIFACT_ID)).thenReturn(ARTIFACT_NAME);
        shape = new ArtifactDotShape(node);
    }

    @Test
    public void testGetId() throws Exception {
        assertEquals("N1", shape.getId());
    }

    @Test
    public void testGetShape() throws Exception {
        assertEquals(NodeShape.box, shape.getShape());
    }

    @Test
    public void testGetLabel() throws Exception {
        assertEquals(ARTIFACT_NAME, shape.getLabel());
    }

    @Test
    public void testToString() throws Exception {
        assertEquals("N1 [ label=\"" + ARTIFACT_NAME + "\" style=\"box\" ]", shape.toString());
    }

}
