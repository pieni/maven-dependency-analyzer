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

import org.neo4j.graphdb.Node;

import java.util.Collections;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: pieter
 * Date: 26-1-11
 * Time: 22:21
 * To change this template use File | Settings | File Templates.
 */
public class OptimizedGroupDotShape extends DotShape {
    private List<Node> containedNodes;

    public boolean contains(Node node) {
        return containedNodes.contains(node);
    }

    public List<Node> getNodes() {
        return Collections.unmodifiableList(containedNodes);
    }

    public void add(Node node) {
        this.containedNodes.add(node);
    }

}
