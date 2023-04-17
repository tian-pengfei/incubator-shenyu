package org.apache.shenyu.client.springmvc.register;

import org.apache.shenyu.client.core.client.ApiBean;
import org.apache.shenyu.client.core.client.extractor.ApiBeansExtractor;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Controller;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class SpringMvcApiBeansExtractor implements ApiBeansExtractor<Object> {

    private final String contextPath;

    public SpringMvcApiBeansExtractor(String contextPath) {
        this.contextPath = contextPath;
    }

    @Override
    public List<ApiBean<Object>> extract(ApplicationContext applicationContext) {
        Map<String, Object> beanMap = applicationContext.getBeansWithAnnotation(Controller.class);

        List<ApiBean<Object>> apiBeans = new ArrayList<>();

        beanMap.forEach((k, v) -> {
            bean2ApiBean(k, v).ifPresent(apiBeans::add);
        });
        return apiBeans;
    }

    private Optional<ApiBean<Object>> bean2ApiBean(String beanName, Object bean) {

        Class<?> targetClass = getCorrectedClass(bean);

        RequestMapping classRequestMapping = AnnotationUtils.findAnnotation(targetClass, RequestMapping.class);

        String beanPath = Objects.isNull(classRequestMapping) ? "" : getPath(classRequestMapping);

        ApiBean<Object> apiBean = new ApiBean<>(contextPath, beanName, bean, beanPath, targetClass);

        final Method[] methods = ReflectionUtils.getUniqueDeclaredMethods(targetClass);

        for (Method method : methods) {

            final RequestMapping methodRequestMapping =
                    AnnotatedElementUtils.findMergedAnnotation(method, RequestMapping.class);
            if (Objects.isNull(methodRequestMapping)) {
                continue;
            }
            apiBean.addApiDefinition(method, getPath(methodRequestMapping));
        }

        return Optional.of(apiBean);
    }

    private Class<?> getCorrectedClass(final Object bean) {

        Class<?> clazz = bean.getClass();
        if (AopUtils.isAopProxy(bean)) {
            clazz = AopUtils.getTargetClass(bean);
        }
        return clazz;
    }

    private String getPath(@NonNull RequestMapping requestMapping) {
        return Optional.of(requestMapping.path()[0]).orElse("");
    }
}
