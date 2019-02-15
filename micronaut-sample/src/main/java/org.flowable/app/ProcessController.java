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
import org.flowable.serverless.ServerlessUtil;
import org.flowable.serverless.Util;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;

@Controller("/process")
public class ProcessController {

    public static ProcessEngine processEngine = ServerlessUtil.initializeProcessEngineForBpmnModel(
        commandContext -> TestProcessExample.createTestProcessExampleBpmnModel());

    @Post(consumes = MediaType.TEXT_PLAIN)
    public String post(@Body String id) {
        Map<String, Object> result = new HashMap<>();

        ProcessInstance processInstance = processEngine.getRuntimeService()
            .createProcessInstanceBuilder()
            .processDefinitionId(ServerlessUtil.PROCESS_DEFINITION_ID)
            .transientVariable("result", result)
            .variable("personId", id)
            .start();

        return "[Micronaut] new process instance " + processInstance.getId() + " : " + result;
    }

}