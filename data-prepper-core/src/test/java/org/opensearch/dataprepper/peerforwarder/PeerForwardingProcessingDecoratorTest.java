/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.dataprepper.peerforwarder;

import com.amazon.dataprepper.model.event.Event;
import com.amazon.dataprepper.model.peerforwarder.RequiresPeerForwarding;
import com.amazon.dataprepper.model.processor.Processor;
import com.amazon.dataprepper.model.record.Record;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PeerForwardingProcessingDecoratorTest {
    private static final String TEST_PLUGIN_ID = "test_plugin_id";
    private static final String TEST_IDENTIFICATION_KEY = "identification_key";

    private Record<Event> record;

    @Mock
    Processor processor;

    @Mock(extraInterfaces = Processor.class)
    RequiresPeerForwarding requiresPeerForwarding;

    @Mock
    PeerForwarder peerForwarder;

    @BeforeEach
    void setUp() {
        record = mock(Record.class);
    }

    private PeerForwardingProcessorDecorator createObjectUnderTest(Processor processor) {
        return new PeerForwardingProcessorDecorator(processor, peerForwarder, TEST_PLUGIN_ID);
    }

    @Test
    void PeerForwardingProcessingDecorator_should_have_interaction_with_getIdentificationKeys() {
        createObjectUnderTest((Processor) requiresPeerForwarding);
        verify(requiresPeerForwarding).getIdentificationKeys();
    }

    @Test
    void PeerForwardingProcessingDecorator_should_not_have_any_interactions_if_its_not_an_instance_of_RequiresPeerForwarding() {
        createObjectUnderTest(processor);
        verifyNoInteractions(processor);
    }

    @Test
    void PeerForwardingProcessingDecorator_execute_should_forwardRecords_with_correct_values() {
        Set<String> identificationKeys = Set.of(TEST_IDENTIFICATION_KEY);
        List<Record<Event>> testData = Collections.singletonList(record);

        when(requiresPeerForwarding.getIdentificationKeys()).thenReturn(identificationKeys);
        when(peerForwarder.forwardRecords(testData, identificationKeys, TEST_PLUGIN_ID)).thenReturn(testData);

        Processor processor = (Processor) requiresPeerForwarding;
        when(processor.execute(testData)).thenReturn(testData);

        final PeerForwardingProcessorDecorator objectUnderTest = createObjectUnderTest(processor);
        final Collection<Record<Event>> records = objectUnderTest.execute(testData);

        verify(requiresPeerForwarding).getIdentificationKeys();
        verify(peerForwarder).forwardRecords(testData, identificationKeys, TEST_PLUGIN_ID);
        Assertions.assertNotNull(records);
        assertThat(records.size(), equalTo(testData.size()));
        assertThat(records, equalTo(testData));
    }

    @Test
    void PeerForwardingProcessingDecorator_execute_will_call_inner_processors_execute() {
        PeerForwardingProcessorDecorator objectUnderTest = createObjectUnderTest(processor);
        Collection<Record<Event>> testData = Collections.singletonList(record);
        objectUnderTest.execute(testData);
        verify(processor).execute(any(Collection.class));
    }

    @Test
    void PeerForwardingProcessingDecorator_prepareForShutdown_will_call_inner_processors_prepareForShutdown() {
        PeerForwardingProcessorDecorator objectUnderTest = createObjectUnderTest(processor);
        objectUnderTest.prepareForShutdown();
        verify(processor).prepareForShutdown();
    }

    @Test
    void PeerForwardingProcessingDecorator_isReadyForShutdown_will_call_inner_processors_isReadyForShutdown() {
        PeerForwardingProcessorDecorator objectUnderTest = createObjectUnderTest(processor);
        objectUnderTest.isReadyForShutdown();
        verify(processor).isReadyForShutdown();
    }

    @Test
    void PeerForwardingProcessingDecorator_shutdown_will_call_inner_processors_shutdown() {
        PeerForwardingProcessorDecorator objectUnderTest = createObjectUnderTest(processor);
        objectUnderTest.shutdown();
        verify(processor).shutdown();
    }

}