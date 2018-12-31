package org.flowable.serverless;

import org.flowable.bpmn.model.BpmnModel;
import org.flowable.common.engine.impl.persistence.deploy.DeploymentCache;
import org.flowable.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.flowable.engine.impl.persistence.deploy.ProcessDefinitionCacheEntry;
import org.flowable.engine.impl.persistence.entity.ProcessDefinitionEntityImpl;
import org.flowable.engine.repository.ProcessDefinition;

/**
 * For demo purposes, quick access to proc def.
 */
public class ServerlessProcessDefinitionUtil {

    public static String PROCESS_DEFINITION_ID = "theProcess";

    public static ProcessDefinition PROCESS_DEFINITION;

    public static void deployServerlessProcessDefinition(BpmnModel bpmnModel, ProcessEngineConfigurationImpl engineConfiguration) {
        PROCESS_DEFINITION = new ProcessDefinitionEntityImpl();
        ((ProcessDefinitionEntityImpl) PROCESS_DEFINITION).setId(PROCESS_DEFINITION_ID);
        ProcessDefinitionCacheEntry cacheEntry = new ProcessDefinitionCacheEntry(PROCESS_DEFINITION, bpmnModel, bpmnModel.getMainProcess());

        DeploymentCache<ProcessDefinitionCacheEntry> processDefinitionCache = engineConfiguration.getProcessDefinitionCache();
        processDefinitionCache.add(PROCESS_DEFINITION_ID, cacheEntry);

    }

}
