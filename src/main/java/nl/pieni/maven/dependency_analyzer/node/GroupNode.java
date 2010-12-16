package nl.pieni.maven.dependency_analyzer.node;

/**
 * Interface for a GroupNode
 */
public interface GroupNode extends DependencyNode {
    /**
     * Get the GroupId
     *
     * @return groupId
     */
    String getGroupId();
}
