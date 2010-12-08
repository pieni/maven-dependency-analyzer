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

package nl.pieni.maven.dependency_graph.filter;

import org.apache.maven.model.Dependency;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: pieter
 * Date: 1-12-10
 * Time: 22:42
 * To change this template use File | Settings | File Templates.
 */
public class DependencyIncludeFilterTest {

    private Dependency junit;
    private Dependency orgPieniMaven;
    private List<Dependency> dependencies = new ArrayList<Dependency>();
    private Dependency junitXx;


    @Test
    public void includeGroupId() {
        List<String> pattern = new ArrayList<String>();
        pattern.add("junit");
        DependencyIncludeFilter filter = new DependencyIncludeFilter(pattern);

        List<Dependency> result = filter.filter(dependencies);
        assertTrue(result.size() == 2);
    }

    @Test
    public void includeGroupIdAndArtifactId() {
        List<String> pattern = new ArrayList<String>();
        pattern.add("junit:junit");
        DependencyIncludeFilter filter = new DependencyIncludeFilter(pattern);

        List<Dependency> result = filter.filter(dependencies);
        assertTrue(result.size() == 1);
        assertTrue(result.get(0).equals(junit));
    }

    @Test
    public void includeEndsWithWildCard() {
        List<String> pattern = new ArrayList<String>();
        pattern.add("org.*");
        DependencyIncludeFilter filter = new DependencyIncludeFilter(pattern);

        List<Dependency> result = filter.filter(dependencies);
        assertTrue(result.size() == 1);
        assertTrue(result.get(0).equals(orgPieniMaven));
    }

    @Test
    public void includeStartsWithWildCard() {
        List<String> pattern = new ArrayList<String>();
        pattern.add("*.maven");
        DependencyIncludeFilter filter = new DependencyIncludeFilter(pattern);

        List<Dependency> result = filter.filter(dependencies);
        assertTrue(result.size() == 1);
        assertTrue(result.get(0).equals(orgPieniMaven));
    }

    @Test
    public void includeWildCardOnly() {
        List<String> pattern = new ArrayList<String>();
        pattern.add("*");
        DependencyIncludeFilter filter = new DependencyIncludeFilter(pattern);

        List<Dependency> result = filter.filter(dependencies);
        assertTrue(result.size() == 3);
        assertTrue(result.equals(dependencies));
    }

    @Test
    public void includeEmptyPatternValue() {
        List<String> pattern = new ArrayList<String>();
        pattern.add("");
        DependencyIncludeFilter filter = new DependencyIncludeFilter(pattern);

        List<Dependency> result = filter.filter(dependencies);
        assertTrue(result.size() == 3);
        assertTrue(result.equals(dependencies));
    }

    @Test
    public void includeEmptyPatternList() {
        List<String> pattern = new ArrayList<String>();
        DependencyIncludeFilter filter = new DependencyIncludeFilter(pattern);

        List<Dependency> result = filter.filter(dependencies);
        assertTrue(result.size() == 0);
    }

    @Test
    public void includeStartAndEndWildCard() {
        List<String> pattern = new ArrayList<String>();
        pattern.add("*.pieni.*");
        DependencyIncludeFilter filter = new DependencyIncludeFilter(pattern);

        List<Dependency> result = filter.filter(dependencies);
        assertTrue(result.size() == 1);
        assertTrue(result.get(0).equals(orgPieniMaven));
    }


    private Dependency getJunitXx45() {
        Dependency dependency = new Dependency();
        dependency.setArtifactId("xx");
        dependency.setGroupId("junit");
        dependency.setVersion("4.5");

        return dependency;
    }

    private Dependency getJunitJunit() {
        Dependency dependency = new Dependency();
        dependency.setArtifactId("junit");
        dependency.setGroupId("junit");
        dependency.setVersion("4.5");

        return dependency;
    }

    private Dependency getOrgPieniMaven10() {
        Dependency dependency = new Dependency();
        dependency.setArtifactId("artifact");
        dependency.setGroupId("org.pieni.maven");
        dependency.setVersion("1.0");

        return dependency;
    }

    @Before
    public void setUp() throws Exception {
        this.junit = getJunitJunit();
        this.junitXx = getJunitXx45();
        this.orgPieniMaven = getOrgPieniMaven10();
        dependencies.add(this.junitXx);
        dependencies.add(junit);
        dependencies.add(orgPieniMaven);
    }
}
