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

import org.flowable.bpmn.model.BpmnModel;
import org.flowable.common.engine.impl.event.FlowableEventSupport;
import org.flowable.common.engine.impl.interceptor.Command;
import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.common.engine.impl.persistence.deploy.DeploymentCache;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.flowable.engine.impl.persistence.deploy.ProcessDefinitionCacheEntry;
import org.flowable.engine.impl.persistence.entity.ProcessDefinitionEntityImpl;
import org.flowable.engine.repository.ProcessDefinition;

/**
 * For demo purposes, quick access to proc def.
 */
public class ServerlessUtil {

    public static String PROCESS_DEFINITION_ID = "theProcess";

    public static ProcessDefinition PROCESS_DEFINITION;

    public static ProcessEngine initializeProcessEngineForBpmnModel(Command<BpmnModel> bpmnModelCommand) {

        long start = System.currentTimeMillis();

        NoDbProcessEngineConfiguration engineConfiguration = new NoDbProcessEngineConfiguration();
        ProcessEngine processEngine = engineConfiguration.buildProcessEngine();

        BpmnModel bpmnModel = processEngine.getManagementService().executeCommand(bpmnModelCommand);

        // TODO: move to processor?
        bpmnModel.setEventSupport(new FlowableEventSupport());

        // This is trickier to move
        Util.processFlowElements(bpmnModel.getMainProcess().getFlowElements(), bpmnModel.getMainProcess());

        // END TODO

        ServerlessUtil.deployServerlessProcessDefinition(bpmnModel, engineConfiguration);

        long end = System.currentTimeMillis();
        System.out.println("Flowable engine booted up in " + (end - start) + " ms");

        return processEngine;
    }

    public static void deployServerlessProcessDefinition(BpmnModel bpmnModel, ProcessEngineConfigurationImpl engineConfiguration) {
        PROCESS_DEFINITION = new ProcessDefinitionEntityImpl();
        ((ProcessDefinitionEntityImpl) PROCESS_DEFINITION).setId(PROCESS_DEFINITION_ID);
        ProcessDefinitionCacheEntry cacheEntry = new ProcessDefinitionCacheEntry(PROCESS_DEFINITION, bpmnModel, bpmnModel.getMainProcess());

        DeploymentCache<ProcessDefinitionCacheEntry> processDefinitionCache = engineConfiguration.getProcessDefinitionCache();
        processDefinitionCache.add(PROCESS_DEFINITION_ID, cacheEntry);

    }

}
