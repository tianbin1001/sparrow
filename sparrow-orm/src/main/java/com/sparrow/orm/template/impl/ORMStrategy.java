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

package com.sparrow.orm.template.impl;

import com.sparrow.enums.DIALECT;
import com.sparrow.enums.STATUS_RECORD;
import com.sparrow.orm.Dialect;
import com.sparrow.orm.query.AGGREGATE;
import com.sparrow.orm.query.SearchCriteria;
import com.sparrow.orm.query.UpdateCriteria;
import com.sparrow.orm.template.SparrowDaoSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author harry
 */
public class ORMStrategy<T, I> implements SparrowDaoSupport<T, I> {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    public ORMStrategy() {
        this(null);
    }

    public ORMStrategy(String schema) {
        DIALECT dialect = Dialect.getInstance(schema).getDialect();
        Class clazz = null;
        Type type = getClass()
            .getGenericSuperclass();
        if (type instanceof ParameterizedType) {
            clazz = (Class<?>) ((ParameterizedType) type).getActualTypeArguments()[0];
        }
        switch (dialect) {
            case MYSQL:
            case SQL_SERVER:
                ormDaoSupport = new DBORMTemplate<T, I>(clazz);
                break;
            case ELASTIC_SEARCH:
                ormDaoSupport = null;
                break;
            default:
                ormDaoSupport = new DBORMTemplate<T, I>(clazz);
        }
    }

    private SparrowDaoSupport<T, I> ormDaoSupport;

    @Override
    public <P, Q> Map<P, Q> getMap(SearchCriteria criteria) {
        return this.ormDaoSupport.getMap(criteria);
    }

    @Override
    public <Z> Set<Z> firstList(SearchCriteria criteria) {
        return this.ormDaoSupport.firstList(criteria);
    }

    @Override
    public Long getCount(SearchCriteria criteria) {
        return this.ormDaoSupport.getCount(criteria);
    }

    @Override
    public <X> X getFieldValue(SearchCriteria criteria) {
        return this.ormDaoSupport.getFieldValue(criteria);
    }

    @Override
    public T getEntity(SearchCriteria criteria) {
        return this.ormDaoSupport.getEntity(criteria);
    }

    @Override
    public List<T> getList(SearchCriteria criteria) {
        return this.ormDaoSupport.getList(criteria);
    }

    @Override
    public <P> P scalar(SearchCriteria criteria) {
        return this.ormDaoSupport.scalar(criteria);
    }

    @Override
    public int delete(SearchCriteria criteria) {
        return this.ormDaoSupport.delete(criteria);
    }

    @Override
    public int update(UpdateCriteria criteria) {
        return this.ormDaoSupport.update(criteria);
    }

    @Override
    public Long insert(T model) {
        return this.ormDaoSupport.insert(model);
    }

    @Override
    public int update(T model) {
        return this.ormDaoSupport.update(model);
    }

    @Override
    public int changeStatus(String ids, STATUS_RECORD status) {
        return this.ormDaoSupport.changeStatus(ids, status);
    }

    @Override
    public int delete(I id) {
        return this.ormDaoSupport.delete(id);
    }

    @Override
    public int batchDelete(String ids) {
        return this.ormDaoSupport.batchDelete(ids);
    }

    @Override
    public T getEntity(I id) {
        return this.ormDaoSupport.getEntity(id);
    }

    @Override
    public T getEntity(Object key, String uniqueKey) {
        return this.ormDaoSupport.getEntity(key, uniqueKey);
    }

    @Override
    public List<T> getList() {
        return this.ormDaoSupport.getList();
    }

    @Override
    public Long getCount(Object key, String uniqueKey) {
        return this.ormDaoSupport.getCount(key, uniqueKey);
    }

    @Override
    public Long getCount(Object key) {
        return this.ormDaoSupport.getCount(key);
    }

    @Override
    public <X> X getAggregate(AGGREGATE aggregate, SearchCriteria searchCriteria) {
        return this.ormDaoSupport.getAggregate(aggregate, searchCriteria);
    }

    @Override
    public <X> X getFieldValue(String fieldName, Object key) {
        return this.ormDaoSupport.getFieldValue(fieldName, key);
    }

    @Override
    public <X> X getFieldValue(String fieldName, Object key, String uniqueKey) {
        return this.ormDaoSupport.getFieldValue(fieldName, key, uniqueKey);
    }

    @Override
    public <X> X getAggregate(String fieldName, AGGREGATE aggregate) {
        return this.ormDaoSupport.getAggregate(fieldName, aggregate);
    }
}
