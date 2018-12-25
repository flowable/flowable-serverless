package org.flowable.sample;

import org.flowable.experimental.bpmn.Bpmn;

/**
 * @author Filip Hrisafov
 */
@Bpmn(resource = "processes/vacationRequest.bpmn20.xml")
@Bpmn(resource = "processes/my-process.bpmn20.xml")
public class ExampleProcesses {

}
