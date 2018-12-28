package experiment;

import java.io.Reader;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.flowable.common.engine.impl.history.HistoryLevel;
import org.flowable.common.engine.impl.interceptor.Command;
import org.flowable.common.engine.impl.interceptor.CommandInterceptor;
import org.flowable.common.engine.impl.persistence.StrongUuidGenerator;
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
}
