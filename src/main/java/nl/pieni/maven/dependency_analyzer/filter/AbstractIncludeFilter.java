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

package nl.pieni.maven.dependency_analyzer.filter;

import org.apache.maven.model.Dependency;

import java.util.List;

/**
 * Abstract base class for pattern matching on artifacts (i.e. the GAV coordinates)
 * (@See http://maven.apache.org/shared/maven-common-artifact-filters/source-repository.html)
 */
public abstract class AbstractIncludeFilter<T, R> {

    //The pattern list
    final List<String> patterns;

    AbstractIncludeFilter(List<String> patterns) {
        this.patterns = patterns;
    }

    /**
     * Perform a filtering
     *
     * @param toFilter the Type to filter list of dependencies to filter
     * @return R the result of the filtering
     */
    abstract public R filter(final T toFilter);

    /**
     * Gets whether the specified token matches the specified pattern segment.
     *
     * @param token   the token to check
     * @param pattern the pattern segment to match, as defined above
     * @return <code>true</code> if the specified token is matched by the specified pattern segment
     */
    boolean matches(final String token, final String pattern) {
        boolean matches;

        // support full wildcard and implied wildcard
        if ("*".equals(pattern) || pattern.length() == 0) {
            matches = true;
        }
        // support contains wildcard
        else if (pattern.startsWith("*") && pattern.endsWith("*")) {
            String contains = pattern.substring(1, pattern.length() - 1);

            matches = (token.indexOf(contains) != -1);
        }
        // support leading wildcard
        else if (pattern.startsWith("*")) {
            String suffix = pattern.substring(1, pattern.length());

            matches = token.endsWith(suffix);
        }
        // support trailing wildcard
        else if (pattern.endsWith("*")) {
            String prefix = pattern.substring(0, pattern.length() - 1);

            matches = token.startsWith(prefix);
        }
        // support exact match
        else {
            matches = token.equals(pattern);
        }

        return matches;
    }
}
