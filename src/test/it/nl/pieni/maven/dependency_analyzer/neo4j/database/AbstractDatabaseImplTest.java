/*
 * Copyright (c) 2010 Pieter van der Meer (pieter@pieni.nl)
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

package nl.pieni.maven.dependency_analyzer.neo4j.database;

import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.logging.Log;

import java.io.File;
import java.io.IOException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Base class for database unit testing
 */
public abstract class AbstractDatabaseImplTest {

    private final static String TEST_DB_LOCATION = System.getProperty("java.io.tmpdir") + "/dependency_analyzer_test/";
    static Log log;
    private static int dependencyCnt = 0;

    private static boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    boolean result = file.delete();
                    if (!result) {
                        System.out.println("Unable to delete file = " + file);
                    }
                }
            }
        }
        return (path.delete());
    }

    static void beforeBase() throws IOException {
        //Test and remove any old DB's
        File file = new File(TEST_DB_LOCATION);
        System.out.println("Using directory " + file.getAbsolutePath() + " for test database");
        if (file.exists()) {
            deleteDirectory(file);
        } else {
            boolean result = file.mkdir();
            if (!result) {
                throw new IOException("Unable to create directory");
            }
        }

        log = mock(Log.class);
        when(log.isDebugEnabled()).thenReturn(true);
    }

    static void afterBase() {
        File file = new File(TEST_DB_LOCATION);
        System.out.println("Removing directory " + file.getAbsolutePath() + " (Used for test DB)");
        if (file.exists()) {
            if (!deleteDirectory(file)) {
                System.out.println("Error deleting directory");
            }
        }
    }

    static String getDBDirectory() {
        return TEST_DB_LOCATION;
    }

    Dependency getDependency() {
        Dependency dependency = new Dependency();
        dependency.setArtifactId("artifactId_" + dependencyCnt);
        dependency.setGroupId("groupId_" + dependencyCnt);
        dependency.setVersion("1.0");
        dependency.setType("jar");
        dependencyCnt++;
        return dependency;
    }
}
