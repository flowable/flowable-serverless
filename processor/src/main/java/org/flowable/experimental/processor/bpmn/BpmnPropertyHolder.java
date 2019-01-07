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
package org.flowable.experimental.processor.bpmn;

/**
 * @author Filip Hrisafov
 */
public class BpmnPropertyHolder {

    protected final String resource;
    protected final boolean enableEagerExecutionTreeFetching;

    public BpmnPropertyHolder(String resource, boolean enableEagerExecutionTreeFetching) {
        this.resource = resource;
        this.enableEagerExecutionTreeFetching = enableEagerExecutionTreeFetching;
    }

    public String getResource() {
        return resource;
    }

    public boolean isEnableEagerExecutionTreeFetching() {
        return enableEagerExecutionTreeFetching;
    }

    public static class Builder {

        protected String resource;
        protected boolean enableEagerExecutionTreeFetching;

        public Builder resource(String resource) {
            this.resource = resource;
            return this;
        }

        public Builder enableEagerExecutionTreeFetching(boolean enableEagerExecutionTreeFetching) {
            this.enableEagerExecutionTreeFetching = enableEagerExecutionTreeFetching;
            return this;
        }

        public boolean hasResource() {
            return resource != null;
        }

        public BpmnPropertyHolder create() {
            return new BpmnPropertyHolder(resource, enableEagerExecutionTreeFetching);
        }
    }
}
