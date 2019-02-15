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
package org.flowable.sample;

import org.flowable.serverless.ServerlessUtil;

import experiment.MyJavaDelegate;

public class ProcessService {

	public String startProcess() {
		String processInstanceId = SpringFuApplication.processEngine.getRuntimeService().startProcessInstanceById(ServerlessUtil.PROCESS_DEFINITION_ID).getId();
		return "[Spring Cloud] - new process instance " + processInstanceId + " started. Number of delegation executions = " + MyJavaDelegate.COUNTER.get();
	}

}
