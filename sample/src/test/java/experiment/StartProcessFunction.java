package experiment;

import org.flowable.bpmn.model.BpmnModel;
import org.flowable.common.engine.impl.event.FlowableEventSupport;
import org.flowable.common.engine.impl.persistence.deploy.DeploymentCache;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.impl.persistence.deploy.ProcessDefinitionCacheEntry;
import org.flowable.engine.impl.persistence.entity.ProcessDefinitionEntityImpl;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.sample.SimpleServiceTask;

/**
 * The purpose of this experiment is to build a simple 'function' that starts a process definition.
 *
 * To boot up fast, we're using a custom proc engines config that doesn't have any persistency and disables MyBatis completely.
 * Also, the process should be ready as-is and not need any extra parsing, which means all behaviours are already set (TODO: discuss this point).
 *
 * This leads to the following problems though:
 *
 * - A normal deployment (through the repositoryService.createDeployment) can't happen, as there is no persistency.
 *   So the process definition is passed into the cache directly (which currently needs some hacking like mimicking a ProcessDefinition)
 *
 * - When starting a process instance, the process definition can only be started by id
 *   (by key would trigger a lookup in the data store)
 *
 * - TODO: Currently, the behaviours are set in the element in a hardcoded way.
 *         It might be better to actually deploy the process to an in-mem db and get the parsed behaviors from it.
 *         This might be too costly for annotation processing, probably.
 *         Alternatively, the parsers should be changed to be reusable.
 */
public class StartProcessFunction {

    public static ProcessEngine processEngine;

    public static String PROCESS_DEFINITION_ID = "theProcess";

    public static ProcessDefinition PROCESS_DEFINITION;

    static {

        long start = System.currentTimeMillis();

        NoDbProcessEngineConfiguration engineConfiguration = new NoDbProcessEngineConfiguration();
        processEngine = engineConfiguration.buildProcessEngine();

        BpmnModel bpmnModel = SimpleServiceTask.createSimpleServiceTaskBpmnModel();

        // TODO: move to processor?
        bpmnModel.setEventSupport(new FlowableEventSupport());

        // This is trickier to move
        Util.processFlowElements(bpmnModel.getMainProcess().getFlowElements(), bpmnModel.getMainProcess());

        // END TODO

        PROCESS_DEFINITION = new ProcessDefinitionEntityImpl();
        ((ProcessDefinitionEntityImpl) PROCESS_DEFINITION).setId(PROCESS_DEFINITION_ID);
        ProcessDefinitionCacheEntry cacheEntry = new ProcessDefinitionCacheEntry(PROCESS_DEFINITION, bpmnModel, bpmnModel.getMainProcess());

        DeploymentCache<ProcessDefinitionCacheEntry> processDefinitionCache = engineConfiguration.getProcessDefinitionCache();
        processDefinitionCache.add(PROCESS_DEFINITION_ID, cacheEntry);

        long end = System.currentTimeMillis();
        System.out.println("Engine booted up in " + (end - start) + " ms");
        // TODO: bootup time can be improved
    }

    public static void main(String[] args) {
        int nrOfInstances = 100000;
        long start = System.currentTimeMillis();
        for (int i = 0; i < nrOfInstances; i++) {
            processEngine.getRuntimeService().startProcessInstanceById(PROCESS_DEFINITION_ID);
        }
        long end = System.currentTimeMillis();

        if (MyJavaDelegate.COUNTER.get() != nrOfInstances) {
            throw new RuntimeException("Error: invalid number invocations of delegate: " + MyJavaDelegate.COUNTER.get());
        } else {
            long time = end - start;
            System.out.println("Started " + nrOfInstances + " processes in " + time + " ms, which is " + ( (double) time / (double) nrOfInstances ) + " ms per instance (and with only one thread)");
        }
    }

}
