package com.back.global.aspect;

import com.back.global.rsData.RsData;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class ResponseAspect {

    @Around("""
                execution(public com.back.global.rsData.RsData *(..)) &&
                (
                    within(@org.springframework.stereotype.Controller *) ||
                    within(@org.springframework.web.bind.annotation.RestController *)
                ) &&
                (
                    @annotation(org.springframework.web.bind.annotation.GetMapping) ||
                    @annotation(org.springframework.web.bind.annotation.PostMapping) ||
                    @annotation(org.springframework.web.bind.annotation.PutMapping) ||
                    @annotation(org.springframework.web.bind.annotation.DeleteMapping) ||
                    @annotation(org.springframework.web.bind.annotation.RequestMapping)
                )
            """)
    public Object handleResponse(ProceedingJoinPoint joinPoint) throws Throwable {
        Object proceed = joinPoint.proceed();

        RsData<?> rsData = (RsData<?>) proceed;
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getResponse();
        if (response != null) {
            response.setStatus(rsData.code());
        }

        return proceed;
    }
}


