package io.manbang.easybytecoder.plugin.springplugin.modify.runtime;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;


/**
 * @author xujie
 */
@Slf4j
@Aspect
@Component
public class ArgumentPrintAspect {
    @Around("(@within(org.springframework.stereotype.Service) || @within(org.springframework.stereotype.Repository) || @within(org.springframework.stereotype.Controller))) " +
            "&& execution(public * *(..))")
    public Object printIoArgument(ProceedingJoinPoint point) throws Throwable {
        log.info("【入参】 {}", point.getArgs());
        Object result = point.proceed();
        log.info("【出参】 {}", result);
        return result;
    }
}
