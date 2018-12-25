package org.flowable.sample;

import static org.xmlunit.assertj.XmlAssert.assertThat;

import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.junit.jupiter.api.Test;
import org.xmlunit.builder.Input;

/**
 * @author Filip Hrisafov
 */
class MyProcessTest {

    @Test
    void xmlMatches() {
        BpmnXMLConverter xmlConverter = new BpmnXMLConverter();
        String myProcessXML = new String(xmlConverter.convertToXML(MyProcess.createMyProcessBpmnModel()));
        assertThat(myProcessXML)
            .and(Input.fromStream(getClass().getClassLoader().getResourceAsStream("processes/my-process.bpmn20.xml")))
            .areSimilar();

    }
}
