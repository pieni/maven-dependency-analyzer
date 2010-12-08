/*
 * Copyright  2010 Pieter van der Meer (pieter@pieni.nl)
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
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Include filter for the list of dependencies supplied.
 * (See http://maven.apache.org/shared/maven-common-artifact-filters/source-repository.html)
 */
public class DependencyIncludeFilter {
    //The pattern list
    private final List<String> patterns;

    /**
     * Default constructor
     * @param includePatterns patters to include
     */
    public DependencyIncludeFilter(final List<String> includePatterns) {
        this.patterns = includePatterns;
    }

    /**
     * Perform a filtering
     * @param dependencies list of dependencies to filter
     * @return filtered list
     */
    @NotNull
    public List<Dependency> filter(@NotNull final List<Dependency> dependencies) {
        List<Dependency> result = new ArrayList<Dependency>();
        for (Dependency gav : dependencies) {
            boolean add = include(gav);
            if (add) {
                result.add(gav);
            }
        }
        return result;
    }


    /**
     * include this dependency?
     * @param dependency the dependency
     * @return true when inclusion required
     */
    private boolean include(@NotNull final Dependency dependency) {
        for (String pattern : patterns) {
            if (include(dependency, pattern)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check dependency against the inclusion pattern
     * @param dependency a {@link org.apache.maven.model.Dependency}
     * @param pattern the pattern
     * @return true when inclusion required
     */
    private boolean include( @NotNull final Dependency dependency, @NotNull final String pattern )
    {
        String[] tokens = new String[] {
            dependency.getGroupId(),
            dependency.getArtifactId(),
        };

        String[] patternTokens = pattern.split( ":" );

        // fail immediately if pattern tokens outnumber tokens to match
        boolean matched = ( patternTokens.length <= tokens.length );

        for ( int i = 0; matched && i < patternTokens.length; i++ )
        {
            matched = matches( tokens[i], patternTokens[i] );
        }

        return matched;
    }

    /**
     * Gets whether the specified token matches the specified pattern segment.
     *
     * @param token
     *            the token to check
     * @param pattern
     *            the pattern segment to match, as defined above
     * @return <code>true</code> if the specified token is matched by the specified pattern segment
     */
    private boolean matches( @NotNull final String token, @NotNull final String pattern )
    {
        boolean matches;

        // support full wildcard and implied wildcard
        if ( "*".equals( pattern ) || pattern.length() == 0 )
        {
            matches = true;
        }
        // support contains wildcard
        else if ( pattern.startsWith( "*" ) && pattern.endsWith( "*" ) )
        {
            String contains = pattern.substring( 1, pattern.length() - 1 );

            matches = ( token.indexOf( contains ) != -1 );
        }
        // support leading wildcard
        else if ( pattern.startsWith( "*" ) )
        {
            String suffix = pattern.substring( 1, pattern.length() );

            matches = token.endsWith( suffix );
        }
        // support trailing wildcard
        else if ( pattern.endsWith( "*" ) )
        {
            String prefix = pattern.substring( 0, pattern.length() - 1 );

            matches = token.startsWith( prefix );
        }
        // support exact match
        else
        {
            matches = token.equals( pattern );
        }

        return matches;
    }
}
