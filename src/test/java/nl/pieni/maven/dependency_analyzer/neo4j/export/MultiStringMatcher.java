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

package nl.pieni.maven.dependency_analyzer.neo4j.export;

import org.mockito.ArgumentMatcher;

/**
 * Created by IntelliJ IDEA.
 * User: pieter
 * Date: 18-1-11
 * Time: 17:25
 * To change this template use File | Settings | File Templates.
 */
class MultiStringMatcher extends ArgumentMatcher<String> {
    private String[] matchList;

    public MultiStringMatcher(String[] matchList) {
        this.matchList = matchList;
    }

    @Override
    public boolean matches(Object arguments) {
        String string = (String)arguments;
        for (String s : matchList) {
            if (!string.contains(s)) {
                return false;
            }
        }
        return true;
    }
}
