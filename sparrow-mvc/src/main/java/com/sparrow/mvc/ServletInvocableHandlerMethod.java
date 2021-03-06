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

package com.sparrow.mvc;

import com.sparrow.constant.magic.DIGIT;
import com.sparrow.mvc.resolver.impl.HandlerMethodArgumentResolverComposite;
import com.sparrow.mvc.result.MethodReturnValueResolverHandler;
import com.sparrow.web.support.MethodParameter;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.List;

/**
 * @author harry
 */
public class ServletInvocableHandlerMethod {
    /**
     * 登录类型
     */
    private int loginType = DIGIT.ZERO;
    /**
     * 是否为json返回
     */
    private boolean json = false;
    /**
     * 出错返回的默认url
     */
    private String errorUrl;
    /**
     * 成功返回的默认url
     */
    private String successUrl;
    /**
     * 具体的执行方法
     */
    private Method method;
    /**
     * action url
     */
    private String actionName;
    /**
     * 权限验证
     */
    private boolean validatePrivilege;
    /**
     * 是否需要权限验证
     */
    private boolean validateRequest = true;
    /**
     * 参数名称列表
     */
    private List<String> parameterNameList;
    /**
     * 返回值类型
     */
    private Class returnType;
    /**
     * 具体的controller对象
     */
    private Object controller;
    /**
     * 具体的 controller 类
     */
    private Class<?> controllerClazz;
    /**
     * 方法参封装列表
     */
    private MethodParameter[] methodParameters;

    /**
     * action的参数解析对象 参数会多个
     */
    private HandlerMethodArgumentResolverComposite handlerMethodArgumentResolverComposite;
    /**
     * action 的返回值解析对象 只有一个
     */
    private MethodReturnValueResolverHandler methodReturnValueResolverHandler;

    public Class getReturnType() {
        return returnType;
    }

    public boolean isValidateRequest() {
        return validateRequest;
    }

    public void setValidateRequest(boolean validateRequest) {
        this.validateRequest = validateRequest;
    }

    public List<String> getParameterNameList() {
        return parameterNameList;
    }

    public void setParameterNameList(List<String> parameterNameList) {
        this.parameterNameList = parameterNameList;
    }

    public String getSuccessUrl() {
        return successUrl;
    }

    public void setSuccessUrl(String successUrl) {
        this.successUrl = successUrl;
    }

    public String getErrorUrl() {
        return errorUrl;
    }

    public void setErrorUrl(String errorUrl) {
        this.errorUrl = errorUrl;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
        this.returnType = this.method.getReturnType();
        this.methodParameters = this.initMethodParameters();
    }

    public String getActionName() {
        return actionName;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    public int getLoginType() {
        return loginType;
    }

    public void setLoginType(int loginType) {
        this.loginType = loginType;
    }

    public boolean isValidatePrivilege() {
        return validatePrivilege;
    }

    public void setValidatePrivilege(boolean validatePrivilege) {
        this.validatePrivilege = validatePrivilege;
    }

    public boolean isJson() {
        return json;
    }

    public void setJson(boolean ajax) {
        this.json = ajax;
    }

    private MethodParameter[] initMethodParameters() {
        Class<?>[] parameterClass = this.method
            .getParameterTypes();
        if (parameterClass == null || parameterClass.length == 0) {
            return null;
        }
        int count = parameterClass.length;
        List<String> parameterNameList = this.parameterNameList;
        if (parameterNameList == null || parameterNameList.size() == 0) {
            return null;
        }
        MethodParameter[] result = new MethodParameter[count];
        for (int i = 0; i < count; i++) {
            result[i] = new MethodParameter(method, i, parameterClass[i], parameterNameList.get(i));
        }
        return result;
    }

    public Object invokeAndHandle(FilterChain chain, HttpServletRequest request,
        HttpServletResponse response) throws Exception {
        Object[] args = getMethodArgumentValues(request);
        Object returnValue = this.method.invoke(this.controller, args);
        methodReturnValueResolverHandler.resolve(this, returnValue, chain, request, response);
        return returnValue;
    }

    private Object[] getMethodArgumentValues(HttpServletRequest request) throws Exception {
        MethodParameter[] parameters = this.methodParameters;
        if (this.methodParameters == null || this.methodParameters.length == 0) {
            return null;
        }
        Object[] args = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            MethodParameter parameter = parameters[i];
            if (this.handlerMethodArgumentResolverComposite.supportsParameter(parameter)) {
                args[i] = this.handlerMethodArgumentResolverComposite.resolveArgument(
                    parameter, this, request);
            }
        }
        return args;
    }

    public void setReturnType(Class returnType) {
        this.returnType = returnType;
    }

    public Object getController() {
        return controller;
    }

    public void setController(Object controller) {
        this.controller = controller;
        this.controllerClazz = controller.getClass();
    }

    public Class<?> getControllerClazz() {
        return controllerClazz;
    }

    public MethodParameter[] getMethodParameters() {
        return methodParameters;
    }

    public HandlerMethodArgumentResolverComposite getHandlerMethodArgumentResolverComposite() {
        return handlerMethodArgumentResolverComposite;
    }

    public void setHandlerMethodArgumentResolverComposite(
        HandlerMethodArgumentResolverComposite handlerMethodArgumentResolverComposite) {
        this.handlerMethodArgumentResolverComposite = handlerMethodArgumentResolverComposite;
    }

    public MethodReturnValueResolverHandler getMethodReturnValueResolverHandler() {
        return methodReturnValueResolverHandler;
    }

    public void setMethodReturnValueResolverHandler(MethodReturnValueResolverHandler methodReturnValueResolverHandler) {
        this.methodReturnValueResolverHandler = methodReturnValueResolverHandler;
    }
}
