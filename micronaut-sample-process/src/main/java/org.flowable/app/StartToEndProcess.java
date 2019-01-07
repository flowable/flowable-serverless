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

import org.flowable.experimental.bpmn.Bpmn;

/**
 * The annotation processor from Micronaut seems to disable the one from Flowable, if they are in the same project.
 * Hence that the process is moved to a separate module.
 */
@Bpmn(resource = "processes/start-to-end.bpmn20.xml")
public class StartToEndProcess {

}
