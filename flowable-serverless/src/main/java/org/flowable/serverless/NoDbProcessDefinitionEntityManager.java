package org.flowable.serverless;

import java.util.List;
import java.util.Map;

import org.flowable.engine.impl.ProcessDefinitionQueryImpl;
import org.flowable.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.flowable.engine.impl.persistence.entity.ProcessDefinitionEntityManager;
import org.flowable.engine.repository.ProcessDefinition;

public class NoDbProcessDefinitionEntityManager implements ProcessDefinitionEntityManager {

    @Override
    public ProcessDefinitionEntity findLatestProcessDefinitionByKey(String processDefinitionKey) {
        throw new UnsupportedOperationException();
    }
    @Override
    public ProcessDefinitionEntity findLatestProcessDefinitionByKeyAndTenantId(String processDefinitionKey, String tenantId) {
        throw new UnsupportedOperationException();
    }
    @Override
    public ProcessDefinitionEntity findLatestDerivedProcessDefinitionByKey(String processDefinitionKey) {
        throw new UnsupportedOperationException();
    }
    @Override
    public ProcessDefinitionEntity findLatestDerivedProcessDefinitionByKeyAndTenantId(String processDefinitionKey, String tenantId) {
        throw new UnsupportedOperationException();
    }
    @Override
    public List<ProcessDefinition> findProcessDefinitionsByQueryCriteria(ProcessDefinitionQueryImpl processDefinitionQuery) {
        throw new UnsupportedOperationException();
    }
    @Override
    public long findProcessDefinitionCountByQueryCriteria(ProcessDefinitionQueryImpl processDefinitionQuery) {
        throw new UnsupportedOperationException();
    }
    @Override
    public ProcessDefinitionEntity findProcessDefinitionByDeploymentAndKey(String deploymentId, String processDefinitionKey) {
        throw new UnsupportedOperationException();
    }
    @Override
    public ProcessDefinitionEntity findProcessDefinitionByDeploymentAndKeyAndTenantId(String deploymentId, String processDefinitionKey, String tenantId) {
        throw new UnsupportedOperationException();
    }
    @Override
    public ProcessDefinition findProcessDefinitionByKeyAndVersionAndTenantId(String processDefinitionKey, Integer processDefinitionVersion,
        String tenantId) {
        throw new UnsupportedOperationException();
    }
    @Override
    public List<ProcessDefinition> findProcessDefinitionsByNativeQuery(Map<String, Object> parameterMap) {
        throw new UnsupportedOperationException();
    }
    @Override
    public long findProcessDefinitionCountByNativeQuery(Map<String, Object> parameterMap) {
        throw new UnsupportedOperationException();
    }
    @Override
    public void updateProcessDefinitionTenantIdForDeployment(String deploymentId, String newTenantId) {
        throw new UnsupportedOperationException();
    }
    @Override
    public void deleteProcessDefinitionsByDeploymentId(String deploymentId) {
        throw new UnsupportedOperationException();
    }
    @Override
    public ProcessDefinitionEntity create() {
        throw new UnsupportedOperationException();
    }
    @Override
    public ProcessDefinitionEntity findById(String entityId) {
        return (ProcessDefinitionEntity) ServerlessProcessDefinitionUtil.PROCESS_DEFINITION;
    }
    @Override
    public void insert(ProcessDefinitionEntity entity) {
        throw new UnsupportedOperationException();
    }
    @Override
    public void insert(ProcessDefinitionEntity entity, boolean fireCreateEvent) {
        throw new UnsupportedOperationException();
    }
    @Override
    public ProcessDefinitionEntity update(ProcessDefinitionEntity entity) {
        throw new UnsupportedOperationException();
    }
    @Override
    public ProcessDefinitionEntity update(ProcessDefinitionEntity entity, boolean fireUpdateEvent) {
        throw new UnsupportedOperationException();
    }
    @Override
    public void delete(String id) {
        throw new UnsupportedOperationException();
    }
    @Override
    public void delete(ProcessDefinitionEntity entity) {
        throw new UnsupportedOperationException();
    }
    @Override
    public void delete(ProcessDefinitionEntity entity, boolean fireDeleteEvent) {
        throw new UnsupportedOperationException();
    }
}