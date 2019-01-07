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
package org.flowable.serverless;

import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.flowable.bpmn.model.Activity;
import org.flowable.bpmn.model.BaseElement;
import org.flowable.bpmn.model.BoundaryEvent;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.FlowNode;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.SequenceFlow;
import org.flowable.bpmn.model.SubProcess;

/**
 * Temporary. Taken from BpmnXMLConverter.
 */
public class Util {

    public static void processFlowElements(Collection<FlowElement> flowElementList, BaseElement parentScope) {
        for (FlowElement flowElement : flowElementList) {
            if (flowElement instanceof SequenceFlow) {
                SequenceFlow sequenceFlow = (SequenceFlow) flowElement;
                FlowNode sourceNode = getFlowNodeFromScope(sequenceFlow.getSourceRef(), parentScope);
                if (sourceNode != null) {
                    sourceNode.getOutgoingFlows().add(sequenceFlow);
                    sequenceFlow.setSourceFlowElement(sourceNode);
                }

                FlowNode targetNode = getFlowNodeFromScope(sequenceFlow.getTargetRef(), parentScope);
                if (targetNode != null) {
                    targetNode.getIncomingFlows().add(sequenceFlow);
                    sequenceFlow.setTargetFlowElement(targetNode);
                }

            } else if (flowElement instanceof BoundaryEvent) {
                BoundaryEvent boundaryEvent = (BoundaryEvent) flowElement;
                FlowElement attachedToElement = getFlowNodeFromScope(boundaryEvent.getAttachedToRefId(), parentScope);
                if (attachedToElement instanceof Activity) {
                    Activity attachedActivity = (Activity) attachedToElement;
                    boundaryEvent.setAttachedToRef(attachedActivity);
                    attachedActivity.getBoundaryEvents().add(boundaryEvent);
                }

            } else if (flowElement instanceof SubProcess) {
                SubProcess subProcess = (SubProcess) flowElement;
                processFlowElements(subProcess.getFlowElements(), subProcess);
            }
        }
    }

    public static FlowNode getFlowNodeFromScope(String elementId, BaseElement scope) {
        FlowNode flowNode = null;
        if (StringUtils.isNotEmpty(elementId)) {
            if (scope instanceof Process) {
                flowNode = (FlowNode) ((Process) scope).getFlowElement(elementId);
            } else if (scope instanceof SubProcess) {
                flowNode = (FlowNode) ((SubProcess) scope).getFlowElement(elementId);
            }
        }
        return flowNode;
    }

}
