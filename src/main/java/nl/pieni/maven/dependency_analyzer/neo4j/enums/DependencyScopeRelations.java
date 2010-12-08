package nl.pieni.maven.dependency_analyzer.neo4j.enums;

import org.jetbrains.annotations.NotNull;
import org.neo4j.graphdb.RelationshipType;

import java.util.HashMap;
import java.util.Map;

/**
 * The scope of a dependency
 * Implements the J Bloch implementation for String to enum value casting
 */
public enum DependencyScopeRelations implements RelationshipType {

    compile,    //This dependency is needed for compilation of the main source
    test,       //This dependency is needed for compiling and running tests. It is not needed for compiling the main source or running the final artifact.
    runtime,    //This dependency is needed for running the final artifact. It is not needed for compiling the main source or compiling or running the tests.
    provided,   //This dependency is needed for compiling and/or running the artifact but is not necessary to include in the package, because it is provided by the runtime environment - for example, jsp-api.jar is provided by your web application container, so you don't include it in your WEB-INF/lib (for the example of a webapp); or a plugin or optional package that is a prerequisite for your application, but is not bundled with your application.
    system,     //This dependency is required in some phase of your project's lifecycle, but is system-specific. Use of this scope is discouraged: This is considered an "advanced" kind of feature and should only be used when you truly understand all the ramifications of its use, which can be extremely hard if not actually impossible to quantify. This scope by definition renders your build non-portable. It may be necessarry in certain edge cases. The system scope includes the <systemPath> element which points to the physical location of this dependency on the local machine. It is thus used to refer to some artifact expected to be present on the given local machine an not in a repository; and whose path may vary machine-to-machine. The systemPath element can refer to environment variables in its path: ${JAVA_HOME} for instance.
    tag;        // <optional />


    @NotNull
    private static final Map<String, DependencyScopeRelations> STRING2ENUM = new HashMap<String, DependencyScopeRelations>();

    static {
        for (DependencyScopeRelations val : values()) {
            STRING2ENUM.put(val.toString(), val);
        }
    }

    /**
     * Gets the enum for a code.
     *
     * @param pcode the code.
     * @return the corresponding enum.
     */
    public static DependencyScopeRelations fromString(final String pcode) {
        return STRING2ENUM.get(pcode);
    }

}
