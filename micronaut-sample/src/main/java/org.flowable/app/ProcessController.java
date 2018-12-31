package org.flowable.app;

import org.flowable.bpmn.model.BpmnModel;
import org.flowable.common.engine.impl.event.FlowableEventSupport;
import org.flowable.engine.ProcessEngine;
import org.flowable.serverless.NoDbProcessEngineConfiguration;
import org.flowable.serverless.ServerlessProcessDefinitionUtil;
import org.flowable.serverless.Util;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;

/**
 * Try with curl localhost:8080/process
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

        BpmnModel bpmnModel = org.flowable.app.StartToEnd.createStartToEndBpmnModel();

        // TODO: move to processor?
        bpmnModel.setEventSupport(new FlowableEventSupport());

        // This is trickier to move
        Util.processFlowElements(bpmnModel.getMainProcess().getFlowElements(), bpmnModel.getMainProcess());

        // END TODO

        ServerlessProcessDefinitionUtil.deployServerlessProcessDefinition(bpmnModel, engineConfiguration);

        long end = System.currentTimeMillis();
        System.out.println("Flowable Engine booted up in " + (end - start) + " ms");
    }

    @Get("/") 
    @Produces(MediaType.APPLICATION_JSON)
    public String index() {
        return "[Micronaut] new process instance " + processEngine.getRuntimeService().startProcessInstanceById(ServerlessProcessDefinitionUtil.PROCESS_DEFINITION_ID).getId();
    }
}