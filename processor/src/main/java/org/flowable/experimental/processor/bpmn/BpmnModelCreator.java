package org.flowable.experimental.processor.bpmn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;

import org.flowable.bpmn.model.BoundaryEvent;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.Event;
import org.flowable.bpmn.model.ExtensionAttribute;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.Gateway;
import org.flowable.bpmn.model.HasExtensionAttributes;
import org.flowable.bpmn.model.ManualTask;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.ReceiveTask;
import org.flowable.bpmn.model.ScriptTask;
import org.flowable.bpmn.model.SequenceFlow;
import org.flowable.bpmn.model.StartEvent;
import org.flowable.bpmn.model.Task;
import org.flowable.bpmn.model.UserTask;
import org.flowable.common.engine.api.FlowableIllegalArgumentException;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

/**
 * @author Filip Hrisafov
 */
public class BpmnModelCreator {

    protected final ProcessingEnvironment environment;

    public BpmnModelCreator(ProcessingEnvironment environment) {
        this.environment = environment;
    }

    public TypeSpec createType(BpmnModel bpmnModel) {
        String mainProcessId = bpmnModel.getMainProcess().getId();

        String cleanProcessName = capitalize(cleanNameFromId(mainProcessId));
        MethodSpec.Builder bpmnModelMethodBuilder = MethodSpec.methodBuilder("create" + cleanProcessName + "BpmnModel")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .returns(BpmnModel.class)
            .addStatement("$1T bpmnModel = new $1T()", BpmnModel.class)
            .addCode("\n");

        Map<String, MethodSpec> sharedMethods = new HashMap<>();
        List<Process> processes = bpmnModel.getProcesses();
        Collection<MethodSpec> processMethods = new ArrayList<>(processes.size());
        for (Process process : processes) {
            MethodSpec processMethodSpec = createProcess(process, sharedMethods);
            processMethods.add(processMethodSpec);
            bpmnModelMethodBuilder.addStatement("bpmnModel.addProcess($N())", processMethodSpec);
        }

        MethodSpec bpmnModelMethod = bpmnModelMethodBuilder
            .addCode("\n")
            .addStatement("return bpmnModel")
            .build();
        return TypeSpec.classBuilder(cleanProcessName)
            .addModifiers(Modifier.PUBLIC)
            .addMethod(bpmnModelMethod)
            .addMethods(processMethods)
            .addMethods(sharedMethods.values())
            .build();
    }

    protected MethodSpec createProcess(Process process, Map<String, MethodSpec> sharedMethods) {
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("create" + capitalize(cleanNameFromId(process.getId())) + "Process")
            .addModifiers(Modifier.PROTECTED, Modifier.STATIC)
            .returns(Process.class)
            .addStatement("$1T process = new $1T()", Process.class)
            .addCode("\n")
            .addStatement("process.setId($S)", process.getId())
            .addStatement("process.setName($S)", process.getName())
            .addStatement("process.setExecutable($L)", process.isExecutable())
            .addStatement("process.setDocumentation($S)", process.getDocumentation())
            .addStatement("process.setXmlRowNumber($L)", process.getXmlRowNumber())
            .addStatement("process.setXmlColumnNumber($L)", process.getXmlColumnNumber())
            .addCode("\n");

        // TODO ioSpecification
        // TODO executionListeners
        // TODO lanes

        // TODO flowElementList

        for (FlowElement flowElement : process.getFlowElements()) {
            addFlowElement(flowElement, methodBuilder, "process", sharedMethods);
        }
        methodBuilder.addCode("\n");

        // TODO dataObjects
        // TODO artifactList
        // TODO eventListeners
        if (!process.getCandidateStarterUsers().isEmpty()) {
            for (String candidateStarterUser : process.getCandidateStarterUsers()) {
                methodBuilder.addStatement("process.getCandidateStarterUsers().add($S)", candidateStarterUser);
            }
            methodBuilder.addCode("\n");

        }

        if (!process.getCandidateStarterGroups().isEmpty()) {
            for (String candidateStarterGroup : process.getCandidateStarterGroups()) {
                methodBuilder.addStatement("process.getCandidateStarterGroups().add($S)", candidateStarterGroup);
            }
            methodBuilder.addCode("\n");
        }

        if (!process.getAttributes().isEmpty()) {
            MethodSpec createExtensionAttributeSpec = sharedMethods.computeIfAbsent("createExtensionAttribute", key -> createExtensionAttributeMethod());
            addExtensionAttributes(process, methodBuilder, "process", createExtensionAttributeSpec);
            methodBuilder.addCode("\n");
        }

        return methodBuilder
            .addStatement("return process")
            .build();
    }

    protected MethodSpec createExtensionAttributeMethod() {
        return MethodSpec.methodBuilder("createExtensionAttribute")
            .addModifiers(Modifier.PROTECTED, Modifier.STATIC)
            .returns(ExtensionAttribute.class)
            .addParameter(String.class, "name")
            .addParameter(String.class, "value")
            .addParameter(String.class, "namespacePrefix")
            .addParameter(String.class, "namespace")
            .addStatement("$1T extensionAttribute = new $1T(name)", ExtensionAttribute.class)
            .addStatement("extensionAttribute.setValue(value)")
            .addStatement("extensionAttribute.setNamespacePrefix(namespacePrefix)")
            .addStatement("extensionAttribute.setNamespace(namespacePrefix)")
            .addStatement("return extensionAttribute")
            .build();
    }

    protected void addExtensionAttributes(HasExtensionAttributes hasExtensionAttributes, MethodSpec.Builder methodBuilder, String elementName,
        MethodSpec createExtensionAttributeSpec) {
        for (Map.Entry<String, List<ExtensionAttribute>> entry : hasExtensionAttributes.getAttributes().entrySet()) {
            for (ExtensionAttribute extensionAttribute : entry.getValue()) {
                methodBuilder
                    .addStatement("$L.addAttribute($N($S, $S, $S, $S))", elementName, createExtensionAttributeSpec, extensionAttribute.getName(),
                        extensionAttribute.getValue(), extensionAttribute.getNamespacePrefix(), extensionAttribute.getNamespace());
            }
        }

    }

    protected void addFlowElement(FlowElement flowElement, MethodSpec.Builder methodBuilder, String elementName, Map<String, MethodSpec> sharedMethods) {
        if (flowElement instanceof Event) {
            addEventFlowElement((Event) flowElement, methodBuilder, elementName, sharedMethods);
        } else if (flowElement instanceof SequenceFlow) {
            addSequenceFlowElement((SequenceFlow) flowElement, methodBuilder, elementName, sharedMethods);
        } else if (flowElement instanceof Task) {
            addTaskFlowElement((Task) flowElement, methodBuilder, elementName, sharedMethods);
        } else if (flowElement instanceof Gateway) {
            addGatewayFlowElement((Gateway) flowElement, methodBuilder, elementName, sharedMethods);
        } else {
            throw new FlowableIllegalArgumentException("Unsupported flow type " + flowElement.getClass());
        }
    }

    protected void addEventFlowElement(Event event, MethodSpec.Builder methodBuilder, String elementName, Map<String, MethodSpec> sharedMethods) {
        if (event instanceof StartEvent) {
            StartEvent startEvent = (StartEvent) event;
            MethodSpec createStartEventMethodSpec = sharedMethods.computeIfAbsent("createStartEventFlowElement", this::createStartEventFlowElement);
            methodBuilder.addStatement("$L.addFlowElement($N($S, $S, $S, $L, $L, $S, $S, $L, $L, $L))",
                elementName, createStartEventMethodSpec, startEvent.getId(), startEvent.getName(), startEvent.getDocumentation(), startEvent.isAsynchronous(),
                startEvent.isExclusive(), startEvent.getInitiator(), startEvent.getFormKey(), startEvent.isInterrupting(), startEvent.getXmlRowNumber(),
                startEvent.getXmlColumnNumber());
        } else if (event instanceof BoundaryEvent) {
            throw new IllegalArgumentException("BoundaryEvent not implemented yet");
        } else {
            Class<? extends Event> eventCLass = event.getClass();
            MethodSpec createEventMethodSpec = sharedMethods
                .computeIfAbsent("create" + eventCLass.getSimpleName() + "FlowElement", key -> createBaseEventFlowElement(key, eventCLass));
            methodBuilder.addStatement("$L.addFlowElement($N($S, $S, $S, $L, $L, $L, $L))",
                elementName, createEventMethodSpec, event.getId(), event.getName(), event.getDocumentation(), event.isAsynchronous(),
                event.isExclusive(), event.getXmlRowNumber(), event.getXmlColumnNumber());
        }
    }

    protected void addSequenceFlowElement(SequenceFlow sequenceFlow, MethodSpec.Builder methodBuilder, String elementName,
        Map<String, MethodSpec> sharedMethods) {
        MethodSpec createSequenceFlowMethod = sharedMethods.computeIfAbsent("createSequenceFlow", this::createSequenceFlowElement);
        methodBuilder.addStatement("$L.addFlowElement($N($S, $S, $S, $S, $S, $S, $S, $L, $L))",
            elementName, createSequenceFlowMethod, sequenceFlow.getId(), sequenceFlow.getName(), sequenceFlow.getDocumentation(),
            sequenceFlow.getConditionExpression(), sequenceFlow.getSourceRef(), sequenceFlow.getTargetRef(), sequenceFlow.getSkipExpression(),
            sequenceFlow.getXmlRowNumber(), sequenceFlow.getXmlColumnNumber());
    }

    protected void addTaskFlowElement(Task taskFlow, MethodSpec.Builder methodBuilder, String elementName,
        Map<String, MethodSpec> sharedMethods) {
        if (taskFlow instanceof ScriptTask) {
            MethodSpec createScriptTaskMethod = sharedMethods.computeIfAbsent("createScriptTaskFlow", this::createScriptTaskFlowElement);
            ScriptTask scriptTask = (ScriptTask) taskFlow;
            methodBuilder.addStatement("$L.addFlowElement($N($S, $S, $S, $S, $S, $S, $L, $L, $L, $S, $L, $L, $L))",
                elementName, createScriptTaskMethod, scriptTask.getId(), scriptTask.getName(), scriptTask.getDocumentation(),
                scriptTask.getScriptFormat(), scriptTask.getScript(), scriptTask.getResultVariable(),
                scriptTask.isAutoStoreVariables(),
                scriptTask.isAsynchronous(),
                scriptTask.isExclusive(),
                scriptTask.getDefaultFlow(),
                scriptTask.isForCompensation(),
                scriptTask.getXmlRowNumber(), scriptTask.getXmlColumnNumber());
        } else if (taskFlow instanceof UserTask) {
            MethodSpec createUserTaskMethod = sharedMethods.computeIfAbsent("createUserTaskFlow", this::createUserTaskFlowElement);
            UserTask userTaskFlow = (UserTask) taskFlow;
            methodBuilder.addStatement("$L.addFlowElement($N($S, $S, $S, $S, $S, $S, $S, $S, $S, $L, $L, $S, $L, $L, $L))",
                elementName, createUserTaskMethod,
                userTaskFlow.getId(),
                userTaskFlow.getName(),
                userTaskFlow.getDocumentation(),
                userTaskFlow.getAssignee(),
                userTaskFlow.getOwner(),
                userTaskFlow.getPriority(),
                userTaskFlow.getFormKey(),
                userTaskFlow.getDueDate(),
                userTaskFlow.getCategory(),
                userTaskFlow.isAsynchronous(),
                userTaskFlow.isExclusive(),
                userTaskFlow.getDefaultFlow(),
                userTaskFlow.isForCompensation(),
                userTaskFlow.getXmlRowNumber(),
                userTaskFlow.getXmlColumnNumber()
            );
        } else if (taskFlow instanceof ManualTask || taskFlow instanceof ReceiveTask) {
            Class<? extends Task> taskClass = taskFlow.getClass();
            MethodSpec createBaseTaskMethod = sharedMethods
                .computeIfAbsent("create" + taskClass.getSimpleName() + "Flow", key -> createBaseTaskFlowElement(key, taskClass));
            methodBuilder.addStatement("$L.addFlowElement($N($S, $S, $S, $L, $L, $S, $L, $L, $L))",
                elementName, createBaseTaskMethod,
                taskFlow.getId(),
                taskFlow.getName(),
                taskFlow.getDocumentation(),
                taskFlow.isAsynchronous(),
                taskFlow.isExclusive(),
                taskFlow.getDefaultFlow(),
                taskFlow.isForCompensation(),
                taskFlow.getXmlRowNumber(),
                taskFlow.getXmlColumnNumber()
            );
        } else {
            throw new IllegalArgumentException("Task of type " + taskFlow.getClass() + " is not supported");
        }
    }

    protected void addGatewayFlowElement(Gateway gatewayFlow, MethodSpec.Builder methodBuilder, String elementName,
        Map<String, MethodSpec> sharedMethods) {
        Class<? extends Gateway> gatewayClass = gatewayFlow.getClass();
        MethodSpec createBaseGatewayMethod = sharedMethods
            .computeIfAbsent("create" + gatewayClass.getSimpleName() + "Flow", key -> createBaseGatewayFlowElement(key, gatewayClass));
        methodBuilder.addStatement("$L.addFlowElement($N($S, $S, $S, $L, $L, $S, $L, $L))",
            elementName, createBaseGatewayMethod,
            gatewayFlow.getId(),
            gatewayFlow.getName(),
            gatewayFlow.getDocumentation(),
            gatewayFlow.isAsynchronous(),
            gatewayFlow.isExclusive(),
            gatewayFlow.getDefaultFlow(),
            gatewayFlow.getXmlRowNumber(),
            gatewayFlow.getXmlColumnNumber()
        );
    }

    protected MethodSpec createStartEventFlowElement(String methodName) {
        return MethodSpec.methodBuilder(methodName)
            .addModifiers(Modifier.PROTECTED, Modifier.STATIC)
            .returns(StartEvent.class)
            .addParameter(String.class, "id")
            .addParameter(String.class, "name")
            .addParameter(String.class, "documentation")
            .addParameter(boolean.class, "asynchronous")
            .addParameter(boolean.class, "exclusive")
            .addParameter(String.class, "initiator")
            .addParameter(String.class, "formKey")
            .addParameter(boolean.class, "interrupting")
            .addParameter(int.class, "xmlRowNumber")
            .addParameter(int.class, "xmlColumnNumber")
            .addStatement("$1T startEvent = new $1T()", StartEvent.class)
            .addStatement("startEvent.setId(id)")
            .addStatement("startEvent.setName(name)")
            .addStatement("startEvent.setDocumentation(documentation)")
            .addStatement("startEvent.setAsynchronous(asynchronous)")
            .addStatement("startEvent.setExclusive(exclusive)")
            .addStatement("startEvent.setInitiator(initiator)")
            .addStatement("startEvent.setFormKey(formKey)")
            .addStatement("startEvent.setInterrupting(interrupting)")
            .addStatement("startEvent.setXmlRowNumber(xmlRowNumber)")
            .addStatement("startEvent.setXmlColumnNumber(xmlColumnNumber)")
            .addStatement("return startEvent")
            .build();

    }

    protected MethodSpec createBaseEventFlowElement(String methodName, Class<? extends Event> eventClass) {
        return MethodSpec.methodBuilder(methodName)
            .addModifiers(Modifier.PROTECTED, Modifier.STATIC)
            .returns(eventClass)
            .addParameter(String.class, "id")
            .addParameter(String.class, "name")
            .addParameter(String.class, "documentation")
            .addParameter(boolean.class, "asynchronous")
            .addParameter(boolean.class, "exclusive")
            .addParameter(int.class, "xmlRowNumber")
            .addParameter(int.class, "xmlColumnNumber")
            .addStatement("$1T event = new $1T()", eventClass)
            .addStatement("event.setId(id)")
            .addStatement("event.setName(name)")
            .addStatement("event.setDocumentation(documentation)")
            .addStatement("event.setAsynchronous(asynchronous)")
            .addStatement("event.setExclusive(exclusive)")
            .addStatement("event.setXmlRowNumber(xmlRowNumber)")
            .addStatement("event.setXmlColumnNumber(xmlColumnNumber)")
            .addStatement("return event")
            .build();

    }

    protected MethodSpec createSequenceFlowElement(String methodName) {
        return MethodSpec.methodBuilder(methodName)
            .addModifiers(Modifier.PROTECTED, Modifier.STATIC)
            .returns(SequenceFlow.class)
            .addParameter(String.class, "id")
            .addParameter(String.class, "name")
            .addParameter(String.class, "documentation")
            .addParameter(String.class, "conditionExpression")
            .addParameter(String.class, "sourceRef")
            .addParameter(String.class, "targetRef")
            .addParameter(String.class, "skipExpression")
            .addParameter(int.class, "xmlRowNumber")
            .addParameter(int.class, "xmlColumnNumber")
            .addStatement("$1T startEvent = new $1T(sourceRef, targetRef)", SequenceFlow.class)
            .addStatement("startEvent.setId(id)")
            .addStatement("startEvent.setName(name)")
            .addStatement("startEvent.setDocumentation(documentation)")
            .addStatement("startEvent.setConditionExpression(conditionExpression)")
            .addStatement("startEvent.setXmlRowNumber(xmlRowNumber)")
            .addStatement("startEvent.setXmlColumnNumber(xmlColumnNumber)")
            .addStatement("return startEvent")
            .build();

    }

    protected MethodSpec createScriptTaskFlowElement(String methodName) {
        return MethodSpec.methodBuilder(methodName)
            .addModifiers(Modifier.PROTECTED, Modifier.STATIC)
            .returns(ScriptTask.class)
            .addParameter(String.class, "id")
            .addParameter(String.class, "name")
            .addParameter(String.class, "documentation")
            .addParameter(String.class, "scriptFormat")
            .addParameter(String.class, "script")
            .addParameter(String.class, "resultVariable")
            .addParameter(boolean.class, "autoStoreVariables")
            .addParameter(boolean.class, "asynchronous")
            .addParameter(boolean.class, "exclusive")
            .addParameter(String.class, "defaultFlow")
            .addParameter(boolean.class, "forCompensation")
            .addParameter(int.class, "xmlRowNumber")
            .addParameter(int.class, "xmlColumnNumber")
            .addStatement("$1T scriptTask = new $1T()", ScriptTask.class)
            .addStatement("scriptTask.setId(id)")
            .addStatement("scriptTask.setName(name)")
            .addStatement("scriptTask.setDocumentation(documentation)")
            .addStatement("scriptTask.setScriptFormat(scriptFormat)")
            .addStatement("scriptTask.setScript(script)")
            .addStatement("scriptTask.setResultVariable(resultVariable)")
            .addStatement("scriptTask.setAutoStoreVariables(autoStoreVariables)")
            .addStatement("scriptTask.setAsynchronous(asynchronous)")
            .addStatement("scriptTask.setExclusive(exclusive)")
            .addStatement("scriptTask.setDefaultFlow(defaultFlow)")
            .addStatement("scriptTask.setForCompensation(forCompensation)")
            .addStatement("scriptTask.setXmlRowNumber(xmlRowNumber)")
            .addStatement("scriptTask.setXmlColumnNumber(xmlColumnNumber)")
            .addStatement("return scriptTask")
            .build();
    }

    protected MethodSpec createBaseTaskFlowElement(String methodName, Class<? extends Task> taskClass) {
        return MethodSpec.methodBuilder(methodName)
            .addModifiers(Modifier.PROTECTED, Modifier.STATIC)
            .returns(taskClass)
            .addParameter(String.class, "id")
            .addParameter(String.class, "name")
            .addParameter(String.class, "documentation")
            .addParameter(boolean.class, "asynchronous")
            .addParameter(boolean.class, "exclusive")
            .addParameter(String.class, "defaultFlow")
            .addParameter(boolean.class, "forCompensation")
            .addParameter(int.class, "xmlRowNumber")
            .addParameter(int.class, "xmlColumnNumber")
            .addStatement("$1T task = new $1T()", taskClass)
            .addStatement("task.setId(id)")
            .addStatement("task.setName(name)")
            .addStatement("task.setDocumentation(documentation)")
            .addStatement("task.setAsynchronous(asynchronous)")
            .addStatement("task.setExclusive(exclusive)")
            .addStatement("task.setDefaultFlow(defaultFlow)")
            .addStatement("task.setForCompensation(forCompensation)")
            .addStatement("task.setXmlRowNumber(xmlRowNumber)")
            .addStatement("task.setXmlColumnNumber(xmlColumnNumber)")
            .addStatement("return task")
            .build();
    }

    protected MethodSpec createBaseGatewayFlowElement(String methodName, Class<? extends Gateway> gatewayClass) {
        return MethodSpec.methodBuilder(methodName)
            .addModifiers(Modifier.PROTECTED, Modifier.STATIC)
            .returns(gatewayClass)
            .addParameter(String.class, "id")
            .addParameter(String.class, "name")
            .addParameter(String.class, "documentation")
            .addParameter(boolean.class, "asynchronous")
            .addParameter(boolean.class, "exclusive")
            .addParameter(String.class, "defaultFlow")
            .addParameter(int.class, "xmlRowNumber")
            .addParameter(int.class, "xmlColumnNumber")
            .addStatement("$1T gateway = new $1T()", gatewayClass)
            .addStatement("gateway.setId(id)")
            .addStatement("gateway.setName(name)")
            .addStatement("gateway.setDocumentation(documentation)")
            .addStatement("gateway.setAsynchronous(asynchronous)")
            .addStatement("gateway.setExclusive(exclusive)")
            .addStatement("gateway.setDefaultFlow(defaultFlow)")
            .addStatement("gateway.setXmlRowNumber(xmlRowNumber)")
            .addStatement("gateway.setXmlColumnNumber(xmlColumnNumber)")
            .addStatement("return gateway")
            .build();
    }

    protected MethodSpec createUserTaskFlowElement(String methodName) {
        return MethodSpec.methodBuilder(methodName)
            .addModifiers(Modifier.PROTECTED, Modifier.STATIC)
            .returns(UserTask.class)
            .addParameter(String.class, "id")
            .addParameter(String.class, "name")
            .addParameter(String.class, "documentation")
            .addParameter(String.class, "assignee")
            .addParameter(String.class, "owner")
            .addParameter(String.class, "priority")
            .addParameter(String.class, "formKey")
            .addParameter(String.class, "dueDate")
            .addParameter(String.class, "category")
            .addParameter(boolean.class, "asynchronous")
            .addParameter(boolean.class, "exclusive")
            .addParameter(String.class, "defaultFlow")
            .addParameter(boolean.class, "forCompensation")
            .addParameter(int.class, "xmlRowNumber")
            .addParameter(int.class, "xmlColumnNumber")
            .addStatement("$1T userTask = new $1T()", UserTask.class)
            .addStatement("userTask.setId(id)")
            .addStatement("userTask.setName(name)")
            .addStatement("userTask.setDocumentation(documentation)")
            .addStatement("userTask.setAssignee(assignee)")
            .addStatement("userTask.setOwner(owner)")
            .addStatement("userTask.setPriority(priority)")
            .addStatement("userTask.setFormKey(formKey)")
            .addStatement("userTask.setDueDate(dueDate)")
            .addStatement("userTask.setCategory(category)")
            .addStatement("userTask.setAsynchronous(asynchronous)")
            .addStatement("userTask.setExclusive(exclusive)")
            .addStatement("userTask.setDefaultFlow(defaultFlow)")
            .addStatement("userTask.setForCompensation(forCompensation)")
            .addStatement("userTask.setXmlRowNumber(xmlRowNumber)")
            .addStatement("userTask.setXmlColumnNumber(xmlColumnNumber)")
            .addStatement("return userTask")
            .build();
    }

    protected static String capitalize(String value) {
        if (Character.isUpperCase(value.charAt(0))) {
            return value;
        }

        char[] chars = value.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        return new String(chars);
    }

    protected static String cleanNameFromId(String id) {
        return id.replaceAll("[-_.]", "");
    }
}
