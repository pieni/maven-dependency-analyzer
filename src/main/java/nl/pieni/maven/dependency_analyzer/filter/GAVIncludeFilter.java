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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: pieter
 * Date: 18-1-11
 * Time: 21:36
 * To change this template use File | Settings | File Templates.
 */
public class GAVIncludeFilter extends AbstractIncludeFilter {
    //The pattern list
    private final List<String> patterns;

    /**
     * Default constructor
     *
     * @param includePatterns patters to include
     */
    public GAVIncludeFilter(final List<String> includePatterns) {
        this.patterns = includePatterns;
    }

    public boolean filter(final String gavString) {
        for (String pattern : patterns) {
            if (include(gavString, pattern)) {
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
