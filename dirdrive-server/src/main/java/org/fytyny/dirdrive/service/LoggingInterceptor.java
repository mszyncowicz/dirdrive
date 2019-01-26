package org.fytyny.dirdrive.service;

import org.slf4j.Logger;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.io.Serializable;


@Interceptor
@Loggable
public class LoggingInterceptor implements Serializable
{

    @Inject
    private transient Logger logger;

    @AroundInvoke
    private Object intercept(InvocationContext ic) throws Exception
    {
        logger.info(">>> " + ic.getTarget().getClass().getName() + "-" + ic.getMethod().getName());
        try
        {
            return ic.proceed();
        }
        finally
        {
            logger.info("<<< " + ic.getTarget().getClass().getName() + "-" + ic.getMethod().getName());
        }
    }
}