package org.flowable.sample;

import static org.xmlunit.assertj.XmlAssert.assertThat;

import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.junit.jupiter.api.Test;
import org.xmlunit.builder.Input;

/**
 * @author Filip Hrisafov
 */
class VacationRequestTest {

    @Test
    void xmlMatches() {
        BpmnXMLConverter xmlConverter = new BpmnXMLConverter();
        String vacationRequestXML = new String(xmlConverter.convertToXML(VacationRequest.createVacationRequestBpmnModel()));
        assertThat(vacationRequestXML)
            .and(Input.fromStream(getClass().getClassLoader().getResourceAsStream("processes/vacationRequest.bpmn20.xml")))
            .areSimilar();

    }
}
