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
