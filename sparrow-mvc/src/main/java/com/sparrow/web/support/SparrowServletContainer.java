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

package com.sparrow.web.support;

import com.sparrow.constant.CONSTANT;
import com.sparrow.servlet.impl.AbstractServletContainer;
import com.sparrow.support.ContextHolder;
import com.sparrow.support.HttpContext;
import com.sparrow.support.protocol.pager.PagerResult;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

/**
 * Sparrow framework controller support class that used by sparrow only
 *
 * @author harry
 * @version 1.0
 */
public class SparrowServletContainer extends AbstractServletContainer {

    @Override
    public HttpServletRequest getRequest() {
        return HttpContext.getContext().getRequest();
    }

    @Override
    public HttpServletResponse getResponse() {
        return HttpContext.getContext().getResponse();
    }

    public <T> void grid(String gridView, List<T> list) {
        this.grid(gridView, list, null);
    }

    public <T> void grid(String gridView, List<T> list, PagerResult pagerSearch) {
        ContextHolder.getInstance().put(gridView + ".dataSource", list);
        if (pagerSearch != null) {
            ContextHolder.getInstance().put(gridView + ".recordCount",
                pagerSearch.getRecordCount());
            ContextHolder.getInstance().put("spanRecordCount.innerHTML",
                pagerSearch.getRecordCount());
            ContextHolder.getInstance().put(gridView + ".pageSize",
                pagerSearch.getPageSize());
        }
    }

    @Override
    public void flash(String key, Object value) {
        this.getRequest().getSession()
            .setAttribute(key, value);
    }

    @Override
    public <T> T flash(String key) {
        return (T) this.getRequest().getSession()
            .getAttribute(key);
    }

    @Override
    public <T> T removeFlash(String key) {
        T t = (T) this.getRequest().getSession()
            .getAttribute(key);
        this.getRequest().getSession().removeAttribute(key);
        return t;
    }

    @Override
    public void clear() {
        ContextHolder.getInstance().remove();
    }

    @Override
    public <T> T get(String key) {
        return (T) ContextHolder.getInstance().get(key);
    }

    public void putURLParameter(Object... parameters) {
        ContextHolder.getInstance().put(CONSTANT.ACTION_RESULT_URL_PARAMETERS, Arrays.asList(parameters));
    }
}
