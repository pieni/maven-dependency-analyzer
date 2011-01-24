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

import org.hamcrest.Description;
import org.mockito.ArgumentMatcher;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

/**
 * Matcher for test cases
 */
public class RelationshipTypeMatcher extends ArgumentMatcher<RelationshipType> {

    private final RelationshipType relationshipType;

    public RelationshipTypeMatcher(RelationshipType relationshipType) {
        this.relationshipType = relationshipType;
    }

    @Override
    public boolean matches(Object argument) {
        return argument.equals(relationshipType);
    }

    public void describeTo(Description description) {
        description.appendText("Version matcher ");
        description.appendText("\"" + relationshipType.toString() + "\"");
    }
}
