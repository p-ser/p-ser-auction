package com.pser.auction.filter;

import com.google.common.base.CaseFormat;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CamelCaseFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        final Map<String, String[]> paramters = new ConcurrentHashMap<>();

        // 파라미터의 키 값을 snake_case에서 camelCase 변환 후 맵에 값을 가지고 있음
        for (String param : request.getParameterMap().keySet()) {
            String camelCaseParam = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, param);
            paramters.put(camelCaseParam, request.getParameterValues(param));
            paramters.put(param, request.getParameterValues(param));
        }

        // 필터체인을 이용하여, request에 해당 값을 추가하여 반환
        chain.doFilter(new HttpServletRequestWrapper((HttpServletRequest) request) {

            @Override
            public String getParameter(String name) {
                return paramters.containsKey(name) ? paramters.get(name)[0] : null;
            }

            @Override
            public Enumeration<String> getParameterNames() {
                return Collections.enumeration(paramters.keySet());
            }

            @Override
            public String[] getParameterValues(String name) {
                return paramters.get(name);
            }

            @Override
            public Map<String, String[]> getParameterMap() {
                return paramters;
            }

        }, response);
    }
}
