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

package nl.pieni.maven.dependency_analyzer.matchers;

import nl.pieni.maven.dependency_analyzer.neo4j.node.ArtifactNodeDecorator;
import org.mockito.ArgumentMatcher;

/**
 * Matcher for test cases
 */
public class ArtifactNodeDecoratorMatcher extends ArgumentMatcher<ArtifactNodeDecorator> {
    private final String artifactId;

    public ArtifactNodeDecoratorMatcher(String artifactId) {
        this.artifactId = artifactId;
    }

    @Override
    public boolean matches(Object argument) {
        return artifactId.equals(((ArtifactNodeDecorator)argument).getArtifactId());
    }
}
