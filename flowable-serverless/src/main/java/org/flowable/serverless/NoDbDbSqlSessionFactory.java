package org.flowable.serverless;

import java.sql.Connection;

import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.TransactionIsolationLevel;
import org.flowable.common.engine.impl.context.Context;
import org.flowable.common.engine.impl.db.DbSqlSession;
import org.flowable.common.engine.impl.db.DbSqlSessionFactory;
import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.common.engine.impl.interceptor.Session;
import org.flowable.common.engine.impl.persistence.cache.EntityCache;

public class NoDbDbSqlSessionFactory extends DbSqlSessionFactory {

    public NoDbDbSqlSessionFactory(boolean usePrefixId) {
        super(usePrefixId);
    }

    @Override
    public Session openSession(CommandContext commandContext) {
        return new NoDbDbSqlSession(this, Context.getCommandContext().getSession(EntityCache.class));
    }

    @Override
    public SqlSessionFactory getSqlSessionFactory() {
        return new SqlSessionFactory() {

            @Override
            public SqlSession openSession() {
                return null;
            }
            @Override
            public SqlSession openSession(boolean b) {
                return null;
            }
            @Override
            public SqlSession openSession(Connection connection) {
                return null;
            }
            @Override
            public SqlSession openSession(TransactionIsolationLevel transactionIsolationLevel) {
                return null;
            }
            @Override
            public SqlSession openSession(ExecutorType executorType) {
                return null;
            }
            @Override
            public SqlSession openSession(ExecutorType executorType, boolean b) {
                return null;
            }
            @Override
            public SqlSession openSession(ExecutorType executorType, TransactionIsolationLevel transactionIsolationLevel) {
                return null;
            }
            @Override
            public SqlSession openSession(ExecutorType executorType, Connection connection) {
                return null;
            }
            @Override
            public Configuration getConfiguration() {
                return null;
            }
        };
    }

    public static class NoDbDbSqlSession extends DbSqlSession {

        public NoDbDbSqlSession(DbSqlSessionFactory dbSqlSessionFactory, EntityCache entityCache) {
            super(dbSqlSessionFactory, entityCache);
        }

        @Override
        public void close() {

        }

        @Override
        public void commit() {

        }

        @Override
        public void rollback() {

        }

    }
}
