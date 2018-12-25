package org.flowable.sample;

import static org.xmlunit.assertj.XmlAssert.assertThat;

import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.junit.jupiter.api.Test;
import org.xmlunit.builder.Input;

/**
 * @author Filip Hrisafov
 */
class ProcTwoAsyncTasksTest {

    @Test
    void xmlMatches() {
        BpmnXMLConverter xmlConverter = new BpmnXMLConverter();
        String myProcessXML = new String(xmlConverter.convertToXML(ProcTwoAsyncTasks.createProcTwoAsyncTasksBpmnModel()));
        assertThat(myProcessXML)
            .and(Input.fromStream(getClass().getClassLoader().getResourceAsStream("processes/proc-two-async-tasks.bpmn")))
            .areSimilar();

    }
}
