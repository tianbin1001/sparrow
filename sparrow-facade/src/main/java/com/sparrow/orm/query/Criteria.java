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

package com.sparrow.orm.query;

import com.sparrow.enums.ComparisonOperator;
import com.sparrow.orm.query.impl.SimpleCriteriaField;
import java.util.Arrays;
import java.util.List;

public class Criteria {

    private Criteria(String field, Boolean alias) {
        this.field = new SimpleCriteriaField(field);
        this.alias = alias;
    }

    private CriteriaField field;

    private boolean alias = true;

    private CriteriaEntry criteriaEntry;

    public CriteriaEntry getCriteriaEntry() {
        return criteriaEntry;
    }

    public static Criteria alias(String field) {
        return new Criteria(field, true);
    }

    public boolean isAlias() {
        return alias;
    }

    public static Criteria field(String field) {
        return new Criteria(field, false);
    }

    public Criteria() {
    }

    public Criteria lessThanEqual(Object upperBound) {
        this.criteriaEntry = new CriteriaEntry(ComparisonOperator.LESS_THAN_OR_EQUAL, upperBound);
        return this;
    }

    public Criteria lessThan(Object upperBound) {
        this.criteriaEntry = new CriteriaEntry(ComparisonOperator.LESS_THAN, upperBound);
        return this;
    }

    public Criteria notEqual(Object value) {
        this.criteriaEntry = new CriteriaEntry(ComparisonOperator.NOT_EQUAL, value);
        return this;
    }

    public Criteria equal(Object value) {
        this.criteriaEntry = new CriteriaEntry(ComparisonOperator.EQUAL, value);
        return this;
    }

    public Criteria greaterThanEqual(Object lowerBound) {
        this.criteriaEntry = new CriteriaEntry(ComparisonOperator.GREATER_THAN_OR_EQUAL, lowerBound);
        return this;
    }

    public Criteria greaterThan(Object lowerBound) {
        this.criteriaEntry = new CriteriaEntry(ComparisonOperator.GREATER_THAN, lowerBound);
        return this;
    }

    public Criteria startWith(Object value) {
        this.criteriaEntry = new CriteriaEntry(ComparisonOperator.START_WITH, value);
        return this;
    }

    public Criteria endWith(Object value) {
        this.criteriaEntry = new CriteriaEntry(ComparisonOperator.END_WITH, value);
        return this;
    }

    public Criteria contains(Object value) {
        this.criteriaEntry = new CriteriaEntry(ComparisonOperator.CONTAIN, value);
        return this;
    }

    public Criteria notContains(Object value) {
        this.criteriaEntry = new CriteriaEntry(ComparisonOperator.NOT_CONTAIN, value);
        return this;
    }

    public Criteria in(Object... values) {
        return in(toCollection(values));
    }

    public Criteria in(Iterable<?> values) {
        this.criteriaEntry = new CriteriaEntry(ComparisonOperator.IN, values);
        return this;
    }

    private List<Object> toCollection(Object... values) {
        return Arrays.asList(values);
    }

    public Criteria notIn(Object... values) {
        return notIn(toCollection(values));
    }

    public Criteria notIn(Iterable<?> values) {
        this.criteriaEntry = new CriteriaEntry(ComparisonOperator.NOT_IN, values);
        return this;
    }

    public CriteriaField getField() {
        return this.field;
    }

    public static class CriteriaEntry {

        private ComparisonOperator key;
        private Object value;

        CriteriaEntry(ComparisonOperator key, Object value) {
            this.key = key;
            this.value = value;
        }

        public ComparisonOperator getKey() {
            return key;
        }

        public Object getValue() {
            return value;
        }

        @Override
        public String toString() {
            return "CriteriaEntry{" +
                "key=" + key +
                ", value=" + value +
                '}';
        }
    }
}
