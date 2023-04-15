package org.apache.shenyu.client.core.annotation;

import org.apache.shenyu.client.core.client.ClientRegisterConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(ClientRegisterConfiguration.class)
public @interface EnableClientRegister {

}
