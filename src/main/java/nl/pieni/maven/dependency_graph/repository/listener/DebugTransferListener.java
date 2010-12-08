/*
 * Copyright  2010 Pieter van der Meer (pieter@pieni.nl)
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package nl.pieni.maven.dependency_graph.repository.listener;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.wagon.events.TransferEvent;
import org.apache.maven.wagon.events.TransferListener;
import org.jetbrains.annotations.NotNull;


/**
 * Listener for the Nexus updater
 * Report progress of the index download
 */
public class DebugTransferListener implements TransferListener {
    private long timestamp;
    private long transfer;
    private long progress_block;
    private long lastSizeReport = 0;
    private static final long SIZE_1M = 1024 * 1024;

    private final org.apache.maven.plugin.logging.Log LOG;

    /**
     * Default constructor
     *
     * @param logger stream to write output to
     */
    public DebugTransferListener(final Log logger) {
        this.LOG = logger;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void transferInitiated(TransferEvent transferEvent) {
        // This space left intentionally blank
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void transferStarted(@NotNull TransferEvent transferEvent) {
        timestamp = transferEvent.getTimestamp();
        long totalSize;
        this.transfer = 0;
        this.progress_block = 0;
        LOG.info("transferStarted");
        if ((transferEvent.getEventType() == TransferEvent.TRANSFER_STARTED) /* 1 */
                &&
                (transferEvent.getRequestType() == TransferEvent.REQUEST_GET) /* 5 */) {
            final String message = "Downloading: " + transferEvent.getResource().getName() + " to "
                    + transferEvent.getLocalFile().toString();
            LOG.info(message);
            totalSize = transferEvent.getResource().getContentLength();
            if (totalSize != -1) {
                LOG.info("Start download of " + totalSize + " bytes");
            }
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void transferProgress(TransferEvent transferEvent, @NotNull byte[] buffer, int length) {
        transfer += buffer.length;
        progress_block += buffer.length;
        //Report each Mb
        if ((transfer / SIZE_1M) != this.lastSizeReport) {
            LOG.info("Transferred " + transfer / SIZE_1M + "Mb (" + transfer + " bytes)");
            this.lastSizeReport = transfer / SIZE_1M;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void transferCompleted(@NotNull TransferEvent transferEvent) {
        final double duration = (double) (transferEvent.getTimestamp() - timestamp) / 1000;

        final String message = "Transfer finished. " + transfer / SIZE_1M + "Mb (" + transfer + " bytes) copied in " + duration + " seconds";

        LOG.info(message);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void transferError(@NotNull TransferEvent transferEvent) {
        LOG.info(" Transfer error: " + transferEvent.getException());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void debug(String message) {
        LOG.debug("message = " + message);
    }
}
