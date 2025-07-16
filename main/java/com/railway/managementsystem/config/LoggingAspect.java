package com.railway.managementsystem.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    /**
     * 定义切点，匹配所有Controller类中的所有公共方法
     */
    @Pointcut("within(com.railway.managementsystem..*.controller..*)")
    public void webLog() {
    }

    /**
     * 在方法执行前记录请求信息
     *
     * @param joinPoint 切点
     */
    @Before("webLog()")
    public void doBefore(JoinPoint joinPoint) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            log.info("HTTP_REQUEST --- URL: [{}], METHOD: [{}], IP: [{}], CLASS_METHOD: [{}], ARGS: {}",
                    request.getRequestURL().toString(),
                    request.getMethod(),
                    request.getRemoteAddr(),
                    joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName(),
                    Arrays.toString(joinPoint.getArgs()));
        }
    }

    /**
     * 在方法成功执行后记录响应
     *
     * @param ret 方法返回值
     */
    @AfterReturning(returning = "ret", pointcut = "webLog()")
    public void doAfterReturning(Object ret) {
        // 响应内容可能很大，这里只记录简要信息或不记录，避免日志过大
        log.info("HTTP_RESPONSE --- OK");
    }

    /**
     * 环绕通知，记录方法执行时间
     *
     * @param proceedingJoinPoint
     * @return
     * @throws Throwable
     */
    @Around("webLog()")
    public Object doAround(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = proceedingJoinPoint.proceed();
        long endTime = System.currentTimeMillis();
        log.info("TIME_COST --- [{}] ms", (endTime - startTime));
        return result;
    }

    @AfterThrowing(pointcut = "webLog()", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Exception e) {
        log.error("HTTP_EXCEPTION --- CLASS_METHOD: [{}], EXCEPTION: {}", joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName(), e.getMessage());
    }

}