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
package experiment;

import org.flowable.bpmn.model.BpmnModel;
import org.flowable.common.engine.impl.event.FlowableEventSupport;
import org.flowable.engine.ProcessEngine;
import org.flowable.sample.SimpleServiceTask;
import org.flowable.serverless.NoDbProcessEngineConfiguration;
import org.flowable.serverless.ServerlessProcessDefinitionUtil;
import org.flowable.serverless.Util;

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

    static {

        long start = System.currentTimeMillis();

        NoDbProcessEngineConfiguration engineConfiguration = new NoDbProcessEngineConfiguration();
        processEngine = engineConfiguration.buildProcessEngine();
        System.out.println("Engine built in " + (System.currentTimeMillis() - start) + " ms");

        long bpmnModelStart = System.currentTimeMillis();
        BpmnModel bpmnModel = SimpleServiceTask.createSimpleServiceTaskBpmnModel();

        // TODO: move to processor?
        bpmnModel.setEventSupport(new FlowableEventSupport());

        // This is trickier to move
        Util.processFlowElements(bpmnModel.getMainProcess().getFlowElements(), bpmnModel.getMainProcess());

        // END TODO

        ServerlessProcessDefinitionUtil.deployServerlessProcessDefinition(bpmnModel, engineConfiguration);

        System.out.println("BpmnModel initialized in  " + (System.currentTimeMillis() - bpmnModelStart) + " ms");

        long end = System.currentTimeMillis();
        System.out.println("Engine booted up in " + (end - start) + " ms");
    }

    public static void main(String[] args) {
        int nrOfInstances = 100000;
        long start = System.currentTimeMillis();
        for (int i = 0; i < nrOfInstances; i++) {
            processEngine.getRuntimeService().startProcessInstanceById(ServerlessProcessDefinitionUtil.PROCESS_DEFINITION_ID);
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
