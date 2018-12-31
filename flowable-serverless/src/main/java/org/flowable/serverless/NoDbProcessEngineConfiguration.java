package org.flowable.serverless;

import java.io.Reader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.flowable.common.engine.impl.history.HistoryLevel;
import org.flowable.common.engine.impl.interceptor.Command;
import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.common.engine.impl.interceptor.CommandInterceptor;
import org.flowable.common.engine.impl.interceptor.Session;
import org.flowable.common.engine.impl.interceptor.SessionFactory;
import org.flowable.common.engine.impl.persistence.StrongUuidGenerator;
import org.flowable.common.engine.impl.persistence.cache.EntityCache;
import org.flowable.common.engine.impl.persistence.cache.EntityCacheImpl;
import org.flowable.engine.impl.cfg.StandaloneProcessEngineConfiguration;

public class NoDbProcessEngineConfiguration extends StandaloneProcessEngineConfiguration {

   public NoDbProcessEngineConfiguration() {
       this.usingRelationalDatabase = false;
       this.disableIdmEngine = true;
       this.enableEventDispatcher = false;
       this.isDbHistoryUsed = false;
       this.historyLevel = HistoryLevel.NONE;
       this.performanceSettings.setEnableEagerExecutionTreeFetching(true);
       this.idGenerator = new StrongUuidGenerator();

       this.dbSqlSessionFactory = new NoDbDbSqlSessionFactory(false);
       this.customSessionFactories = Arrays.asList(this.dbSqlSessionFactory); // Needs to be set as initDbSqlSessionFactory won't be hit due to usingRelationalDatabase = false

       // Disabled due to GraalVM (uses reflection)
       this.flowableFunctionDelegates = Collections.emptyList();
       this.expressionEnhancers = Collections.emptyList();
       this.customExpressionEnhancers = Collections.emptyList();
       this.shortHandExpressionFunctions = Collections.emptyList();
   }

    @Override
    public Configuration initMybatisConfiguration(Environment environment, Reader reader, Properties properties) {
        return null;
    }

    @Override
    public void initSqlSessionFactory() {
        // disabled
    }

    @Override
    protected void postProcessEngineInitialisation() {
        // disable post-engine checks, as they require a persistent datastore
    }

    @Override
    public CommandInterceptor createTransactionInterceptor() {
        return null;
    }

    @Override
    public void initTransactionFactory() {

    }

    @Override
    public void initTransactionContextFactory() {
       // no transactions needed
    }

    @Override
    public List<CommandInterceptor> getAdditionalDefaultCommandInterceptors() {
        return null; // no need for bpmn override interceptor
    }

    @Override
    public void initEntityManagers() {
        super.initEntityManagers();

        this.processDefinitionEntityManager = new NoDbProcessDefinitionEntityManager();
    }

    @Override
    public boolean isUsingSchemaMgmt() {
        return false;
    }

    @Override
    public Command<Void> getSchemaManagementCmd() {
        return null;
    }

    // Disable due to GraalVM (uses reflection)

    @Override
    public void initExpressionEnhancers() {

    }

    @Override
    public void initShortHandExpressionFunctions() {

    }

    @Override
    public void initFunctionDelegates() {

    }

    @Override
    public void initScriptingEngines() {

    }

    // Default entityCacheFactory uses reflection

    @Override
    public void initSessionFactories() {
        super.initSessionFactories();

        sessionFactories.put(EntityCache.class, new SessionFactory() {

            @Override
            public Class<?> getSessionType() {
                return EntityCache.class;
            }
            @Override
            public Session openSession(CommandContext commandContext) {
                return new EntityCacheImpl();
            }
        });
    }



}
