package nl.pieni.maven.dependency_graph.repository;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.sonatype.nexus.index.ArtifactInfoGroup;
import org.sonatype.nexus.index.context.UnsupportedExistingLuceneIndexException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Interface to the repository searcher
 */
public interface RepositorySearcher {
    Map<String, ArtifactInfoGroup> searchIndexGrouped(List<String> groupPatterns, ArtifactRepository repository, List<String> packaging) throws IOException, UnsupportedExistingLuceneIndexException;
}
