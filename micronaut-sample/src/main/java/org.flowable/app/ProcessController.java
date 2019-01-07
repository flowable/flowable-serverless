package org.flowable.app;

import java.util.HashMap;
import java.util.Map;

import org.flowable.bpmn.model.BpmnModel;
import org.flowable.common.engine.impl.event.FlowableEventSupport;
import org.flowable.common.engine.impl.interceptor.Command;
import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.serverless.NoDbProcessEngineConfiguration;
import org.flowable.serverless.ServerlessProcessDefinitionUtil;
import org.flowable.serverless.Util;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;

/**
 * Try with
 *
 * curl -X POST -H "Content-Type: text/plain" --data 1 localhost:8080/process
 * curl -X POST -H "Content-Type: text/plain" --data 2 localhost:8080/process
 *
 * To build graalvm image, run ./build-native-image.sh
 */
@Controller("/process")
public class ProcessController {

    public static ProcessEngine processEngine;

    static {

        long start = System.currentTimeMillis();

        NoDbProcessEngineConfiguration engineConfiguration = new NoDbProcessEngineConfiguration();
        processEngine = engineConfiguration.buildProcessEngine();

        BpmnModel bpmnModel = processEngine.getManagementService().executeCommand(new Command<BpmnModel>() {

            @Override
            public BpmnModel execute(CommandContext commandContext) {
                return TestProcessExample.createTestProcessExampleBpmnModel();
            }
        });

        // TODO: move to processor?
        bpmnModel.setEventSupport(new FlowableEventSupport());

        // This is trickier to move
        Util.processFlowElements(bpmnModel.getMainProcess().getFlowElements(), bpmnModel.getMainProcess());

        // END TODO

        ServerlessProcessDefinitionUtil.deployServerlessProcessDefinition(bpmnModel, engineConfiguration);

        long end = System.currentTimeMillis();
        System.out.println("Flowable Engine booted up in " + (end - start) + " ms");
    }

    @Post(consumes = MediaType.TEXT_PLAIN)
    public String post(@Body String id) {
        Map<String, Object> result = new HashMap<>();

        ProcessInstance processInstance = processEngine.getRuntimeService()
            .createProcessInstanceBuilder()
            .processDefinitionId(ServerlessProcessDefinitionUtil.PROCESS_DEFINITION_ID)
            .transientVariable("result", result)
            .variable("personId", id)
            .start();

        return "[Micronaut] new process instance " + processInstance.getId() + " : " + result;
    }

}