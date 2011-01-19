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

package nl.pieni.maven.dependency_analyzer.repository.listener;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.wagon.events.TransferEvent;
import org.apache.maven.wagon.resource.Resource;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.mockito.Matchers.endsWith;
import static org.mockito.Matchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit testing of the transfer listener
 */
public class RepositoryTransferListenerTest {
    private Log log;
    private RepositoryTransferListener listener;

    @Before
    public void before() {
        log = mock(Log.class);
        listener = new RepositoryTransferListener(log);
    }

    /**
     * Code covarage booster, the method is empty
     */
    @Test
    public void transferInitiatedTest() {
        TransferEvent event = mock(TransferEvent.class);
        listener.transferInitiated(event);
    }

    @Test
    public void transferStartedTest() {
        TransferEvent event = mock(TransferEvent.class);
        when(event.getEventType()).thenReturn(TransferEvent.TRANSFER_STARTED);
        when(event.getRequestType()).thenReturn(TransferEvent.REQUEST_GET);
        Resource resource = mock(Resource.class);
        when(event.getResource()).thenReturn(resource);
        when(resource.getName()).thenReturn("resource-name");
        File file = mock(File.class);
        when(event.getLocalFile()).thenReturn(file);
        when(file.toString()).thenReturn("localfile");
        when(resource.getContentLength()).thenReturn(100L);

        listener.transferStarted(event);
        verify(log).info("transferStarted");
        verify(log).info("Downloading: resource-name to localfile");
        verify(log).info("Start download of 100 bytes");
    }

    @Test
    public void transferProgress1MTest() {
        TransferEvent event = mock(TransferEvent.class);
        byte[] buffer = new byte[1024];
        //Send 1M data
        for (int i = 0; i < 1024; i++) {
            listener.transferProgress(event, buffer, 0);
        }
        verify(log).info("Transferred 1Mb (" + 1048576 + " bytes)");
    }

    @Test
    public void transferProgress2MTest() {
        TransferEvent event = mock(TransferEvent.class);
        byte[] buffer = new byte[1024];
        //Send 1M data
        for (int i = 0; i < (1024*2); i++) {
            listener.transferProgress(event, buffer, 0);
        }
        verify(log).info("Transferred 1Mb (" + 1048576 + " bytes)");
        verify(log).info("Transferred 2Mb (" + 2097152 + " bytes)");
    }

    @Test
    public void transferCompletedTest() {
        TransferEvent event = mock(TransferEvent.class);
        listener.transferCompleted(event);
        verify(log).info(startsWith("Transfer finished"));
        verify(log).info(endsWith("seconds"));
    }

    @Test
    public void  transferError() {
        TransferEvent event = mock(TransferEvent.class);
        listener.transferError(event);
        Exception exception = mock(Exception.class);
        when(event.getException()).thenReturn(exception);
        verify(log).info(startsWith("Transfer error: "));
    }

    @Test
    public void debugTest() {
        listener.debug("Some String");
        verify(log).debug("Message = Some String");
    }
}
