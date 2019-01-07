/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
