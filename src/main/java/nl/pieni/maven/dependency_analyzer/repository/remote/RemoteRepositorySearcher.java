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

package nl.pieni.maven.dependency_analyzer.repository.remote;

import nl.pieni.maven.dependency_analyzer.repository.RepositorySearcher;
import nl.pieni.maven.dependency_analyzer.repository.listener.RepositoryTransferListener;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.logging.Log;
import org.sonatype.nexus.index.ArtifactInfoGroup;
import org.sonatype.nexus.index.Field;
import org.sonatype.nexus.index.GroupedSearchRequest;
import org.sonatype.nexus.index.GroupedSearchResponse;
import org.sonatype.nexus.index.MAVEN;
import org.sonatype.nexus.index.NexusIndexer;
import org.sonatype.nexus.index.SearchType;
import org.sonatype.nexus.index.context.IndexCreator;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.nexus.index.context.UnsupportedExistingLuceneIndexException;
import org.sonatype.nexus.index.creator.MinimalArtifactInfoIndexCreator;
import org.sonatype.nexus.index.search.grouping.GAGrouping;
import org.sonatype.nexus.index.updater.IndexUpdateRequest;
import org.sonatype.nexus.index.updater.IndexUpdater;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 */
public class RemoteRepositorySearcher implements RepositorySearcher {
    private final NexusIndexer indexer;
    private final IndexUpdater indexUpdater;
    private final Log log;
    private final File outputDirectory;
    private final boolean allowSnapshots;

    /**
     * Default constructor
     *
     * @param indexer         the Indexer
     * @param indexUpdater    updater
     * @param log             logger
     * @param outputDirectory index storage base directory
     * @param allowSnapshots  snaphots allowed
     */
    public RemoteRepositorySearcher(final NexusIndexer indexer, final IndexUpdater indexUpdater, final Log log, final File outputDirectory, final boolean allowSnapshots) {
        this.indexer = indexer;
        this.indexUpdater = indexUpdater;
        this.log = log;
        this.outputDirectory = outputDirectory;
        this.allowSnapshots = allowSnapshots;
    }


    /**
     * @inheritDoc
     */
    @Override
    public Map<String, ArtifactInfoGroup> searchIndexGrouped( final List<String> groupPatterns,  final ArtifactRepository repository,  final List<String> packaging) throws IOException, UnsupportedExistingLuceneIndexException {

        getIndexingContext(repository);
        updateRemoteIndex();

        BooleanQuery bq = createQuery(groupPatterns, packaging);

        GroupedSearchRequest request = new GroupedSearchRequest(bq, new GAGrouping());

        // Perform the search
        GroupedSearchResponse response = indexer.searchGrouped(request);

        getLog().info("Query resulted in: " + response.getTotalHits() + " hits");
        // Return the artifact info objects
        return response.getResults();
    }


    /**
     * Create the query to perform
     * @param groupPatterns list of patters to search for
     * @param packaging the packinging of the artifact searched for
     * @return {@link BooleanQuery}
     */
    private BooleanQuery createQuery( final List<String> groupPatterns,  final List<String> packaging) {
        BooleanQuery bq = new BooleanQuery();

        Query query;

        Field field = MAVEN.GROUP_ID;
        BooleanQuery groupQuery = new BooleanQuery();
        for (String pattern : groupPatterns) {
            if (pattern.endsWith("*")) {
                query = indexer.constructQuery(field, pattern, SearchType.EXACT);
            } else {
                query = indexer.constructQuery(field, pattern + "*", SearchType.EXACT);
            }
            groupQuery.add(query, BooleanClause.Occur.SHOULD);
        }
        bq.add(groupQuery, BooleanClause.Occur.MUST);

        BooleanQuery packagingQuery = new BooleanQuery();
        for (String pack : packaging) {
            Query q = indexer.constructQuery(MAVEN.PACKAGING, pack, SearchType.EXACT);
            packagingQuery.add(q, BooleanClause.Occur.SHOULD);
        }

        bq.add(packagingQuery, BooleanClause.Occur.MUST);

        Query queryClassifierSources = indexer.constructQuery(MAVEN.CLASSIFIER, "sources", SearchType.EXACT);
        Query queryClassifierJavaDoc = indexer.constructQuery(MAVEN.CLASSIFIER, "javadoc", SearchType.EXACT);

        bq.add(new BooleanClause(queryClassifierJavaDoc, BooleanClause.Occur.MUST_NOT));
        bq.add(new BooleanClause(queryClassifierSources, BooleanClause.Occur.MUST_NOT));
        getLog().info("Created query: " + bq);
        return bq;
    }

    /**
     * Get an indexing context
     * @param repository the repository
     * @throws IOException Error connecting
     * @throws UnsupportedExistingLuceneIndexException Not a Nexus index
     */
    private void getIndexingContext( final ArtifactRepository repository) throws IOException, UnsupportedExistingLuceneIndexException {
        if (getLog().isDebugEnabled()) {
            getLog().debug("Retrieving indexing contexts");
        }

        List<IndexCreator> indexCreators = new ArrayList<IndexCreator>();
        indexCreators.add(new MinimalArtifactInfoIndexCreator()); // list of index creators
        if (useableRepository(repository, allowSnapshots)) {
            getLog().info("Processing repo: " + repository.getId() + " URL: " + repository.getUrl());
            indexer.addIndexingContext(
                    repository.getId(), //Id of the context
                    repository.getId(), //Id of the repository
                    new File(repository.getBasedir() + repository.getId()), // directory containing repository
                    outputDirectory, // directory where index will be stored
                    repository.getUrl(), //"http://nexus.pieni.nl/nexus/content/repositories/public/", // remote repository url
                    repository.getUrl() + "/.index/", //"http://nexus.pieni.nl/nexus/content/repositories/public/.index/", // index update url
                    indexCreators
            );
        }
    }

    /**
     * Can this repo be used
     * @param repository the repo
     * @param allowSnapshots snapshots allowed
     * @return true when valid
     */
    private boolean useableRepository( final ArtifactRepository repository, final boolean allowSnapshots) {
        boolean snapshots = repository.getSnapshots().isEnabled() || allowSnapshots;
        return repository.getReleases().isEnabled() && snapshots;
    }

    /**
     * The logger
     * @return {@link Log}
     */
    private Log getLog() {
        return this.log;
    }

    /**
     * Retrieve the index file from a remote repo
     * @throws IOException error connecting
     */
    private void updateRemoteIndex()
            throws IOException {
        Map<String, IndexingContext> contexts = indexer.getIndexingContexts();
        for (String key : contexts.keySet()) {
            IndexingContext context = contexts.get(key);
            getLog().info("Updating from index from: " + context.getRepositoryUrl());
            IndexUpdateRequest updRequest = new IndexUpdateRequest(context);
            updRequest.setTransferListener(new RepositoryTransferListener(getLog()));
            updRequest.setForceFullUpdate(false);
            indexUpdater.fetchAndUpdateIndex(updRequest);
        }
    }


}
