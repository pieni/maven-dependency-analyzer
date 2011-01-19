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

import java.util.List;

/**
 * Filter for GAV coordinates
 */
public class GAVIncludeFilter extends AbstractIncludeFilter<String, Boolean> {

    /**
     * Default constructor
     *
     * @param includePatterns patters to include
     */
    public GAVIncludeFilter(final List<String> includePatterns) {
        super(includePatterns);
    }

    public Boolean filter(final String toFilter) {
        for (String pattern : patterns) {
            if (include(toFilter, pattern)) {
                return true;
            }
        }
        return false;
    }

    private boolean include(final String gavString, final String pattern) {
        String[] tokens = gavString.split(":");
        String[] patternTokens = pattern.split(":");

        // fail immediately if pattern tokens outnumber tokens to match
        boolean matched = (patternTokens.length <= tokens.length);

        for (int i = 0; matched && i < patternTokens.length; i++) {
            matched = matches(tokens[i], patternTokens[i]);
        }

        return matched;
    }

}
