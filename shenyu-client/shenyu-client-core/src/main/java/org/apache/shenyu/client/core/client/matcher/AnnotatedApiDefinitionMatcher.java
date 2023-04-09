package org.apache.shenyu.client.core.client.matcher;

import org.apache.shenyu.client.core.client.ApiBean;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.annotation.Annotation;

public final class AnnotatedApiDefinitionMatcher<T> extends ApiDefinitionMatcher<T> {

    Class<? extends Annotation> aClass;

    public AnnotatedApiDefinitionMatcher(Class<? extends Annotation> aClass) {
        this.aClass = aClass;
    }

    @Override
    public boolean match(ApiBean<T>.ApiDefinition apiDefinition) {
        return AnnotatedElementUtils
                .isAnnotated(apiDefinition.getApiMethod(), aClass);
    }
}
