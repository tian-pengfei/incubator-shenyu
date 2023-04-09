package org.apache.shenyu.client.core.client.matcher;

import org.apache.shenyu.client.core.client.ApiBean;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;

public final class AnnotatedApiBeanMatcher<T> extends ApiBeanMatcher<T> {

    Class<? extends Annotation> aClass;

    public AnnotatedApiBeanMatcher(Class<? extends Annotation> aClass) {
        this.aClass = aClass;
    }

    @Override
    public boolean match(ApiBean<T> apiBean) {
        return AnnotationUtils
                .isAnnotationDeclaredLocally(aClass, apiBean.getTargetClass());
    }
}
