/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sparrow.datasource;

import com.sparrow.constant.CACHE_KEY;
import com.sparrow.core.Cache;
import com.sparrow.core.spi.ApplicationContext;
import com.sparrow.support.EnvironmentSupport;
import com.sparrow.utility.StringUtility;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import javax.sql.DataSource;

/**
 * getDatasourceConfig 初始化ContextLoaderListener.java 中配置 database identify
 *
 * @author harry
 */
public class DataSourceFactory {

    private static Map<String, DatasourceConfig> datasourceConfigMap = new ConcurrentHashMap<String, DatasourceConfig>();

    private static DataSourceFactory dataSourceFactory = new DataSourceFactory();

    public static DataSourceFactory getInstance() {
        return dataSourceFactory;
    }

    /**
     * 获取数据源
     *
     * @return
     */
    public DataSource getDataSource(String dataSourceKey) {
        if (StringUtility.isNullOrEmpty(dataSourceKey)) {
            dataSourceKey = DatasourceKey.getDefault().getKey();
        }
        return ApplicationContext.getContainer().getBean(dataSourceKey);
    }

    public DataSource getDataSource() {
        return getDataSource(null);
    }

    public DatasourceConfig getDatasourceConfig() {
        return getDatasourceConfig(null);
    }

    /**
     * 读取数据库源 <p> data source key 与 connection url 映射 data source+suffix determine datasource data
     * source+database_split_key determine jdbc template
     *
     * @param dataSourceKey
     * @return
     */
    public DatasourceConfig getDatasourceConfig(String dataSourceKey) {
        if (StringUtility.isNullOrEmpty(dataSourceKey)) {
            dataSourceKey = "sparrow_default";
        }
        if (datasourceConfigMap.containsKey(dataSourceKey)) {
            return datasourceConfigMap.get(dataSourceKey);
        }

        synchronized (this) {
            if (datasourceConfigMap.containsKey(dataSourceKey)) {
                return datasourceConfigMap.get(dataSourceKey);
            }
            DatasourceConfig datasourceConfig = new DatasourceConfig();
            try {
                Properties props = new Properties();

                String filePath = "/" + dataSourceKey
                    + ".properties";
                props.load(EnvironmentSupport.getInstance().getFileInputStream(filePath));

                datasourceConfig.setDriverClassName(props.getProperty("driverClassName"));
                datasourceConfig.setUsername(props.getProperty("username"));
                datasourceConfig.setPassword(props.getProperty("password"));
                datasourceConfig.setUrl(props.getProperty("url"));
                datasourceConfig.setPoolSize(Integer.parseInt(props.getProperty("poolSize")));
            } catch (Exception ignore) {
                throw new RuntimeException(ignore);
            }
            DatasourceKey key = DatasourceKey.parse(dataSourceKey);
            Cache.getInstance().put(CACHE_KEY.DATA_SOURCE_URL_PAIR, datasourceConfig.getUrl(), key);
            datasourceConfigMap.put(dataSourceKey, datasourceConfig);
            return datasourceConfig;
        }
    }

    public DatasourceKey getDatasourceKey(Connection connection) {
        if (connection == null) {
            return null;
        }
        try {
            return Cache.getInstance().get(CACHE_KEY.DATA_SOURCE_URL_PAIR, connection.getMetaData().getURL());
        } catch (SQLException e) {
            return null;
        }
    }
}
