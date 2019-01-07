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

        @Override
        public void flush() {

        }
    }
}
