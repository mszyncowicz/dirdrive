package org.fytyny.dirdrive.service;

import javax.interceptor.InterceptorBinding;
import java.lang.annotation.*;


@InterceptorBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
@Documented
public @interface Loggable
{
}