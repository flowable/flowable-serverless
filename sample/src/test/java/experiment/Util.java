package experiment;

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
