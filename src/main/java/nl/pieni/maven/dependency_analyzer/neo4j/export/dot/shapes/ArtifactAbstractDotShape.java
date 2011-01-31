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

import nl.pieni.maven.dependency_analyzer.neo4j.enums.NodeProperties;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: pieter
 * Date: 26-1-11
 * Time: 22:40
 * To change this template use File | Settings | File Templates.
 */
public class ArtifactAbstractDotShape extends AbstractDotShape {
    public ArtifactAbstractDotShape(Node node, Set<Relationship> relations) {
        super(node, relations);

    }

    @Override
    public String getLabel() {
        return getNode().getProperty(NodeProperties.ARTIFACT_ID).toString();
    }

    public String getId() {
        return ShapeIdPrefix.Artifact.toString() + getNode().getId();
    }
}
