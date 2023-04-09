package org.apache.shenyu.client.core.client;

import org.springframework.lang.NonNull;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ApiBean<T> {

    protected static final String PATH_SEPARATOR = "/";

    private final String beanName;

    private final T beanInstance;

    private final String beanPath;

    private final String contextPath;

    private final Class<?> targetClass;

    List<ApiDefinition> apiDefinitions = new ArrayList<>();

    public ApiBean(String contextPath, String beanName, T beanInstance, String beanPath, Class<?> targetClass) {

        this.contextPath = contextPath;

        this.beanName = beanName;

        this.beanInstance = beanInstance;

        this.beanPath = beanPath;

        this.targetClass = targetClass;
    }

    public ApiDefinition addApiDefinition(Method method, String methodPath) {
        ApiDefinition apiDefinition = new ApiDefinition(method, methodPath);
        apiDefinitions.add(apiDefinition);
        return apiDefinition;
    }

    public List<ApiDefinition> getApiDefinitions() {
        return apiDefinitions;
    }

    public T getBeanInstance() {
        return beanInstance;
    }

    public String getBeanName() {
        return beanName;
    }

    public Class<?> getTargetClass() {
        return targetClass;
    }

    public String getBeanPath() {
        return beanPath;
    }

    public class ApiDefinition {

        private final Method apiMethod;

        private final String methodPath;

        private ApiDefinition(Method apiMethod, String methodPath) {
            this.apiMethod = apiMethod;
            this.methodPath = methodPath;
        }

        public Method getApiMethod() {
            return apiMethod;
        }

        public String getParentPath() {
            return beanPath;
        }

        public String getApiPath() {
            return pathJoin(contextPath, beanPath, methodPath);
        }

        public Class<?> getBeanClass() {
            return targetClass;
        }

        public ApiBean<T> getApiBean() {
            return ApiBean.this;
        }
    }

    private static String pathJoin(@NonNull final String... path) {

        StringBuilder result = new StringBuilder(PATH_SEPARATOR);

        for (String p : path) {
            if (!result.toString().endsWith(PATH_SEPARATOR)) {
                result.append(PATH_SEPARATOR);
            }
            result.append(p.startsWith(PATH_SEPARATOR) ? p.replaceFirst(PATH_SEPARATOR, "") : p);
        }
        return result.toString();
    }
}
