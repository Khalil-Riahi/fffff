package com.projet.freelencetinder.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.Collections;

@Component
public class RequestLoggingFilter implements Filter {
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest r = (HttpServletRequest) req;
        if (r.getRequestURI().startsWith("/api/livrables") && "PUT".equalsIgnoreCase(r.getMethod())) {
            System.out.println("[DEBUG] PUT Request to: " + r.getRequestURI());
            System.out.println("[DEBUG] Query string: " + r.getQueryString());
            Collections.list(r.getHeaderNames())
                    .forEach(h -> System.out.println("[HDR] " + h + ": " + r.getHeader(h)));
        }
        chain.doFilter(req, res);
    }
}
