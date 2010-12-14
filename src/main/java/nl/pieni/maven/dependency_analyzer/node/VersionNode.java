package nl.pieni.maven.dependency_analyzer.node;

import org.apache.maven.model.Dependency;

/**
 * Created by IntelliJ IDEA.
 * User: pieter
 * Date: 11-12-10
 * Time: 19:42
 * To change this template use File | Settings | File Templates.
 */
public interface VersionNode {

    void setDependency(Dependency dependency);
    String getVersion();
    ArtifactNode getParent();
}
