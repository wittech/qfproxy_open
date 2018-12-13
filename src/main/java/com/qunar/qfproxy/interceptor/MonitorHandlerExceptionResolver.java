package com.qunar.qfproxy.interceptor;

import com.qunar.qfproxy.utils.ErrorCodeUtil;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class MonitorHandlerExceptionResolver implements HandlerExceptionResolver {
//    private static final Logger LOGGER = LoggerFactory.getLogger(MonitorHandlerExceptionResolver.class);

    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        ErrorCodeUtil.catchExceptionAndSet(request, response, ex);
        return null;
    }
}
