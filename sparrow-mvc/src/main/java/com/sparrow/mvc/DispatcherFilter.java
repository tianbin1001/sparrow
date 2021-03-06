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

import com.sparrow.constant.CONFIG;
import com.sparrow.constant.CONFIG_KEY_LANGUAGE;
import com.sparrow.constant.CONSTANT;
import com.sparrow.constant.EXTENSION;
import com.sparrow.constant.SPARROW_ERROR;
import com.sparrow.constant.SYS_OBJECT_NAME;
import com.sparrow.constant.USER;
import com.sparrow.constant.magic.DIGIT;
import com.sparrow.constant.magic.SYMBOL;
import com.sparrow.container.Container;
import com.sparrow.core.Pair;
import com.sparrow.core.spi.ApplicationContext;
import com.sparrow.core.spi.JsonFactory;
import com.sparrow.enums.CONTAINER;
import com.sparrow.enums.LOGIN_TYPE;
import com.sparrow.exception.BusinessException;
import com.sparrow.mvc.adapter.HandlerAdapter;
import com.sparrow.mvc.adapter.impl.MethodControllerHandlerAdapter;
import com.sparrow.mvc.mapping.HandlerMapping;
import com.sparrow.mvc.mapping.impl.UrlMethodHandlerMapping;
import com.sparrow.support.ContextHolder;
import com.sparrow.support.HttpContext;
import com.sparrow.support.Login;
import com.sparrow.support.LoginDialog;
import com.sparrow.support.PrivilegeSupport;
import com.sparrow.support.protocol.Result;
import com.sparrow.support.web.CookieUtility;
import com.sparrow.support.web.ServletUtility;
import com.sparrow.utility.Config;
import com.sparrow.utility.StringUtility;
import com.sparrow.utility.web.SparrowServletUtility;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author harry
 */
public class DispatcherFilter implements Filter {

    Logger logger = LoggerFactory.getLogger(DispatcherFilter.class);

    private ServletUtility servletUtility = ServletUtility.getInstance();

    private SparrowServletUtility sparrowServletUtility = SparrowServletUtility.getInstance();

    private FilterConfig config;

    private List<HandlerAdapter> handlerAdapters;

    private List<HandlerMapping> handlerMappings;

    private List<HandlerInterceptor> handlerInterceptorList;

    //private List<HandlerExceptionResolver> handlerExceptionResolvers;

    private Container container;

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
        FilterChain chain) {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        this.handlerInterceptorList = initInterceptors(request);

        if (preHandler(httpRequest, httpResponse)) {
            return;
        }
        ServletInvocableHandlerMethod invocableHandlerMethod = null;
        Object actionReturnValue = null;
        try {
            invocableHandlerMethod = this.getHandler(httpRequest);
            if (!this.validateUser(httpRequest, httpResponse)) {
                return;
            }
            this.initAttribute(httpRequest, httpResponse);
            if (invocableHandlerMethod == null) {
                chain.doFilter(request, response);
            } else {
                HandlerAdapter adapter = this.getHandlerAdapter(invocableHandlerMethod);
                actionReturnValue = adapter.handle(chain, httpRequest, httpResponse, invocableHandlerMethod);

            }
            this.postHandler(httpRequest, httpResponse);
        } catch (Exception e) {
            errorHandler(httpRequest, httpResponse, invocableHandlerMethod, e);
        } finally {
            //页面渲染完成之后执行
            renderJs(request, response, invocableHandlerMethod);
            afterCompletion(httpRequest, httpResponse);
        }
    }

    private void errorHandler(HttpServletRequest httpRequest, HttpServletResponse httpResponse,
        ServletInvocableHandlerMethod invocableHandlerMethod, Exception e) {
        Throwable target = e;
        if (e.getCause() == null) {
            logger.error("e.getCause==null", e);
        } else {
            target = e.getCause();
            logger.error("e.getCause!=null", e.getCause());
        }
        if (invocableHandlerMethod != null) {
            try {
                invocableHandlerMethod.getMethodReturnValueResolverHandler().errorResolve(target, httpRequest, httpResponse);
            } catch (Exception ignore) {
                logger.error("exception resolve error", ignore);
            }
        }
        ContextHolder.getInstance().removeAll();
    }

    private void renderJs(ServletRequest request, ServletResponse response,
        ServletInvocableHandlerMethod invocableHandlerMethod) {
        if (invocableHandlerMethod != null && invocableHandlerMethod.getActionName().endsWith(EXTENSION.JSP)) {
            String key = CONSTANT.ACTION_RESULT_JAVASCRIPT + SYMBOL.UNDERLINE + invocableHandlerMethod.getActionName();
            if (request.getAttribute(key) != null) {
                try {
                    response.getWriter().write(request.getAttribute(key).toString());
                } catch (IOException e) {
                    logger.error("response writer error", e);
                }
            }
        }
    }

    private void afterCompletion(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        for (HandlerInterceptor handlerInterceptor : handlerInterceptorList) {
            try {
                handlerInterceptor.afterCompletion(httpRequest, httpResponse);
            } catch (Exception ex) {
                logger.error(handlerInterceptor.getClass().getName() + "interception after handler error", ex);
            }
        }
    }

    private void postHandler(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        for (HandlerInterceptor handlerInterceptor : handlerInterceptorList) {
            try {
                handlerInterceptor.postHandle(httpRequest, httpResponse);
            } catch (Exception ex) {
                logger.error(handlerInterceptor.getClass().getName() + "interception post handler error", ex);
            }
        }
    }

    private boolean preHandler(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        for (HandlerInterceptor handlerInterceptor : handlerInterceptorList) {
            try {
                if (!handlerInterceptor.preHandle(httpRequest, httpResponse)) {
                    return true;
                }
            } catch (Exception e) {
                logger.error(handlerInterceptor.getClass().getName() + "interception pre handler error", e);
            }
        }
        return false;
    }

    private List<HandlerInterceptor> initInterceptors(ServletRequest request) {
        if (this.handlerInterceptorList != null) {
            return this.handlerInterceptorList;
        }
        Map<String, Object> interceptorList = ApplicationContext.getContainer().getBeanMap(CONTAINER.INTERCEPTOR);
        List<HandlerInterceptor> handlerInterceptorList = new ArrayList<HandlerInterceptor>();
        if (!servletUtility.include(request) && interceptorList != null) {
            for (Object interceptor : interceptorList.values()) {
                handlerInterceptorList.add((HandlerInterceptor) interceptor);
            }
        }
        return handlerInterceptorList;
    }

    private ServletInvocableHandlerMethod getHandler(HttpServletRequest request) throws Exception {
        for (HandlerMapping handlerMapping : this.handlerMappings) {
            ServletInvocableHandlerMethod invocableHandlerMethod = handlerMapping.getHandler(request);
            if (invocableHandlerMethod != null) {
                return invocableHandlerMethod;
            }
        }
        return null;
    }

    private HandlerAdapter getHandlerAdapter(Object handler) throws ServletException {
        for (HandlerAdapter ha : this.handlerAdapters) {
            if (ha.supports(handler)) {
                return ha;
            }
        }
        throw new ServletException("No adapter for handler [" + handler +
            "]: The DispatcherServlet configuration needs to include a HandlerAdapter that supports this handler");
    }

    private void initAttribute(HttpServletRequest request,
        HttpServletResponse response) {
        //初始化 request
        HttpContext.getContext().setRequest(request);
        //初始化 response
        HttpContext.getContext().setResponse(response);

        if (servletUtility.include(request)) {
            return;
        }
        String actionKey = servletUtility.getActionKey(request);
        logger.debug("PARAMETERS:" + servletUtility.getAllParameter(request));
        logger.debug("ACTION KEY:" + actionKey);
        request.setAttribute(CONSTANT.REQUEST_ACTION_CURRENT_FORUM, request.getParameter("forumCode"));
        String rootPath = Config.getValue(CONFIG.ROOT_PATH);
        if (!StringUtility.isNullOrEmpty(rootPath)) {
            request.setAttribute(CONFIG.ROOT_PATH, rootPath);
            request.setAttribute(CONFIG.WEBSITE,
                Config.getValue(CONFIG.WEBSITE));
        }

        String internationalization = Config
            .getValue(CONFIG.INTERNATIONALIZATION);
        if (internationalization != null) {
            String language = request.getParameter(CONFIG.LANGUAGE);
            if (language == null
                || !internationalization.contains(language)) {
                language = Config.getValue(CONFIG.LANGUAGE);
            }
            ContextHolder.getInstance().put(CONSTANT.REQUEST_LANGUAGE, language);
        }

        request.setAttribute(CONFIG.RESOURCE,
            Config.getValue(CONFIG.RESOURCE));

        request.setAttribute(CONFIG.IMAGE_WEBSITE, Config.getValue(CONFIG.IMAGE_WEBSITE));

        String configWebsiteName = Config.getLanguageValue(
            CONFIG_KEY_LANGUAGE.WEBSITE_NAME, Config.getValue(CONFIG.LANGUAGE));
        request.setAttribute(CONFIG_KEY_LANGUAGE.WEBSITE_NAME, configWebsiteName);

        if (configWebsiteName != null) {
            String currentWebsiteName = CookieUtility.get(request.getCookies(),
                CONFIG_KEY_LANGUAGE.WEBSITE_NAME);
            if (!configWebsiteName.equals(currentWebsiteName)) {
                CookieUtility.set(response, CONFIG_KEY_LANGUAGE.WEBSITE_NAME,
                    configWebsiteName, DIGIT.ALL);
            }
        }
        if (request.getQueryString() != null) {
            request.setAttribute("hdnPreUrl.value", request.getQueryString());
        }

        Pair<String, Map<String, Object>> sessionPair = (Pair<String, Map<String, Object>>) request.getSession().getAttribute(CONSTANT.ACTION_RESULT_FLASH_KEY);
        if (sessionPair == null) {
            return;
        }
        if (StringUtility.matchUrl(sessionPair.getFirst(), actionKey)) {
            Map<String, Object> values = sessionPair.getSecond();
            for (String key : values.keySet()) {
                request.setAttribute(key, values.get(key));
            }
            return;
        }
        //url换掉时，则session 被清空 （非include）
        request.getSession().removeAttribute(CONSTANT.ACTION_RESULT_FLASH_KEY);

    }

    private boolean validateUser(
        HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException, BusinessException {
        if (servletUtility.include(httpRequest)) {
            return true;
        }
        ServletInvocableHandlerMethod handlerExecutionChain = null;
        try {
            handlerExecutionChain = this.getHandler(httpRequest);
        } catch (Exception e) {
            logger.error("mapping handler error", e);
            return false;
        }

        if (handlerExecutionChain == null) {
            return true;
        }

        String actionName = handlerExecutionChain.getActionName();
        Login user = CookieUtility.getUser(httpRequest);
        httpRequest.setAttribute(USER.ID, user.getUserId());
        httpRequest.setAttribute(USER.LOGIN_NAME, user.getUserName());
        if (handlerExecutionChain.getLoginType() == LOGIN_TYPE.NO_LOGIN.ordinal()) {
            return true;
        }

        if (user.getUserId().equals(USER.VISITOR_ID)) {
            String rootPath = Config.getValue(CONFIG.ROOT_PATH);
            if (handlerExecutionChain.getLoginType() == LOGIN_TYPE.MESSAGE.ordinal()) {
                Map<String, Object> map = new HashMap<String, Object>();
                Result result = new Result(SPARROW_ERROR.USER_NOT_LOGIN.getCode(), SPARROW_ERROR.USER_NOT_LOGIN.getMessage());
                httpResponse.getWriter().write(JsonFactory.getProvider().toString(result));
                return false;
            }

            String loginUrl = Config.getValue(CONFIG.LOGIN_TYPE_KEY
                .get(handlerExecutionChain.getLoginType()));
            boolean isInFrame = handlerExecutionChain.getLoginType() == LOGIN_TYPE.LOGIN_IFRAME
                .ordinal();
            if (!StringUtility.isNullOrEmpty(loginUrl)) {
                String defaultSystemPage = rootPath + Config.getValue(CONFIG.DEFAULT_SYSTEM_INDEX);
                String defaultMenuPage = rootPath + Config.getValue(CONFIG.DEFAULT_MENU_PAGE);
                String redirectUrl = httpRequest.getRequestURL().toString();
                if (redirectUrl.endsWith(EXTENSION.DO) || redirectUrl.endsWith(EXTENSION.JSON)) {
                    redirectUrl = servletUtility.referer(httpRequest);
                }
                if (redirectUrl != null && redirectUrl.equals(defaultMenuPage)) {
                    redirectUrl = SYMBOL.EMPTY;
                }

                if (!StringUtility.isNullOrEmpty(redirectUrl)) {
                    if (httpRequest.getQueryString() != null) {
                        redirectUrl += SYMBOL.QUESTION_MARK + httpRequest.getQueryString();
                    }
                    if (isInFrame) {
                        redirectUrl = defaultSystemPage + SYMBOL.QUESTION_MARK + redirectUrl;
                    }
                    loginUrl = loginUrl + SYMBOL.QUESTION_MARK + redirectUrl;
                }
            }
            loginUrl = rootPath + loginUrl;
            if (!handlerExecutionChain.isJson()) {
                if (isInFrame) {
                    ContextHolder.getInstance().execute(String.format("window.parent.location.href='%1$s'", loginUrl));
                } else {
                    httpResponse.sendRedirect(loginUrl);
                }
            } else {
                LoginDialog loginDialog = new LoginDialog(false, false, loginUrl, isInFrame);
                httpResponse.getWriter().write(JsonFactory.getProvider().toString(loginDialog));
            }
            logger.info("login false{}", actionName);
            //如果权限验证失败则移出属性
            this.sparrowServletUtility.moveAttribute(httpRequest);
            return false;
        }

        if (!handlerExecutionChain.isValidatePrivilege()) {
            return true;
        }

        PrivilegeSupport privilegeService = ApplicationContext.getContainer().getBean(
            SYS_OBJECT_NAME.PRIVILEGE_SERVER);
        String forumCode = httpRequest.getParameter("forumCode");

        if (!privilegeService.accessible(
            CookieUtility.getUser(httpRequest).getUserId(), actionName,
            forumCode)) {
            httpResponse.getWriter().write(CONSTANT.ACCESS_DENIED);
            this.sparrowServletUtility.moveAttribute(httpRequest);
            return false;
        }
        ContextHolder.getInstance().put(CONSTANT.REQUEST_USER_ID, user.getUserId());
        return true;
    }

    /**
     * 初始化所有策略
     */
    private void initStrategies() {
        this.initHandlerMapping();
        this.initAdapter();
    }

    /**
     * 初始化handler mapping
     */
    private void initHandlerMapping() {
        this.handlerMappings = new ArrayList<HandlerMapping>();
        this.handlerMappings.add(new UrlMethodHandlerMapping());
    }

    /**
     * 初始化适配器
     */
    private void initAdapter() {
        this.handlerAdapters = new ArrayList<HandlerAdapter>();
        MethodControllerHandlerAdapter adapter = new MethodControllerHandlerAdapter();
        this.handlerAdapters.add(adapter);
    }

    @Override
    public void init(FilterConfig config) throws ServletException {
        this.config = config;
        this.container = ApplicationContext.getContainer();
        this.initStrategies();
    }

    public FilterConfig getConfig() {
        return config;
    }

    public void setConfig(FilterConfig config) {
        this.config = config;
    }
}
