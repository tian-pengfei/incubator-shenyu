package org.apache.shenyu.client.springmvc.config;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.shenyu.client.apidocs.annotations.ApiDoc;
import org.apache.shenyu.client.apidocs.annotations.ApiModule;
import org.apache.shenyu.client.core.client.ApiBean;
import org.apache.shenyu.client.core.client.ApiRegisterDocket;
import org.apache.shenyu.client.core.client.ClientInfoRefreshedEventListener;
import org.apache.shenyu.client.core.client.ContextApiRefreshedEventListener;
import org.apache.shenyu.client.core.client.extractor.ApiBeansExtractor;
import org.apache.shenyu.client.core.client.matcher.AnnotatedApiBeanMatcher;
import org.apache.shenyu.client.core.client.matcher.AnnotatedApiDefinitionMatcher;
import org.apache.shenyu.client.core.client.matcher.ApiBeanMatcher;
import org.apache.shenyu.client.core.client.matcher.Matcher;
import org.apache.shenyu.client.core.client.parser.Parser;
import org.apache.shenyu.client.core.constant.ShenyuClientConstants;
import org.apache.shenyu.client.core.disruptor.ShenyuClientRegisterEventPublisher;
import org.apache.shenyu.client.springmvc.annotation.ShenyuSpringMvcClient;
import org.apache.shenyu.common.enums.ApiHttpMethodEnum;
import org.apache.shenyu.common.enums.ApiSourceEnum;
import org.apache.shenyu.common.enums.ApiStateEnum;
import org.apache.shenyu.common.enums.RpcTypeEnum;
import org.apache.shenyu.common.utils.UriUtils;
import org.apache.shenyu.register.client.api.ShenyuClientRegisterRepository;
import org.apache.shenyu.register.common.config.PropertiesConfig;
import org.apache.shenyu.register.common.config.ShenyuClientConfig;
import org.apache.shenyu.register.common.dto.ApiDocRegisterDTO;
import org.apache.shenyu.register.common.dto.MetaDataRegisterDTO;
import org.apache.shenyu.register.common.enums.EventType;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Controller;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
public class SpringMvcRegisterConfig {


    private final boolean addPrefixed;

    private final boolean isFull;

    private final String contextPath;

    private final String appName;

    private final PropertiesConfig clientConfig;


    public SpringMvcRegisterConfig(final ShenyuClientConfig clientConfig) {

        this.clientConfig = clientConfig.getClient().get(RpcTypeEnum.HTTP.getName());

        Properties props = this.clientConfig.getProps();

        this.addPrefixed = Boolean.parseBoolean(props.getProperty(ShenyuClientConstants.ADD_PREFIXED,
                Boolean.FALSE.toString()));

        this.isFull = Boolean.parseBoolean(props.getProperty(ShenyuClientConstants.IS_FULL, Boolean.FALSE.toString()));

        this.contextPath = Optional.ofNullable(props
                .getProperty(ShenyuClientConstants.CONTEXT_PATH))
                .map(UriUtils::repairData).orElse("");

        this.appName = props.getProperty(ShenyuClientConstants.APP_NAME);
    }

    @Bean
    public ClientInfoRefreshedEventListener clientInfoEventListener(ShenyuClientRegisterEventPublisher publisher) {
        return new ClientInfoRefreshedEventListener(clientConfig, publisher, RpcTypeEnum.HTTP);
    }

    @Bean
    public ContextApiRefreshedEventListener<Object> apiEventListener(ApiBeansExtractor<Object> extractor,
                                                                     ShenyuClientRegisterEventPublisher publisher) {

        return new ApiRegisterDocket<>(extractor, publisher)

                .apiMetaBeanMatcher(apiMetaBeanMatcher())
                .apiMetaMatcher(apiMetaMatcher())
                .apiMetaParser(apiMetaParser())

                .apiDocBeanMatcher(apiDocBeanMatcher())
                .apiDocMatcher(apiDocMatcher())
                .apiDocParser(apiDocParser()).buildApiRefreshedEventListener();
    }

    @Bean
    public ApiBeansExtractor<Object> apiBeansExtractor() {
        return this::getBeans;
    }

    @Bean
    public ShenyuClientRegisterEventPublisher publisher(final ShenyuClientRegisterRepository shenyuClientRegisterRepository) {
        ShenyuClientRegisterEventPublisher publisher = ShenyuClientRegisterEventPublisher.getInstance();
        publisher.start(shenyuClientRegisterRepository);
        return publisher;
    }

    public ApiBeanMatcher<Object> apiMetaBeanMatcher() {
        return new AnnotatedApiBeanMatcher<>(ShenyuSpringMvcClient.class);
    }

    public Matcher<ApiBean<Object>.ApiDefinition> apiMetaMatcher() {
        return new AnnotatedApiDefinitionMatcher<>(ShenyuSpringMvcClient.class);
    }

    public Parser<List<MetaDataRegisterDTO>, ApiBean<Object>.ApiDefinition> apiMetaParser() {
        return this::apiDefinition2ApiMeta;
    }

    public Matcher<ApiBean<Object>> apiDocBeanMatcher() {
        return new AnnotatedApiBeanMatcher<>(ApiModule.class);
    }

    public Matcher<ApiBean<Object>.ApiDefinition> apiDocMatcher() {
        return new AnnotatedApiDefinitionMatcher<>(ApiDoc.class);
    }

    public Parser<List<ApiDocRegisterDTO>, ApiBean<Object>.ApiDefinition> apiDocParser() {
        return this::apiDefinition2ApiDoc;
    }

    private List<ApiBean<Object>> getBeans(final ApplicationContext context) {


        Map<String, Object> beanMap = context.getBeansWithAnnotation(Controller.class);

        List<ApiBean<Object>> apiBeans = new ArrayList<>();

        beanMap.forEach((k, v) -> {
            bean2ApiBean(k, v).ifPresent(apiBeans::add);
        });
        return apiBeans;
    }


    private Optional<ApiBean<Object>> bean2ApiBean(String beanName, Object bean) {

        Class<?> targetClass = getCorrectedClass(bean);

        RequestMapping classRequestMapping = AnnotationUtils.findAnnotation(targetClass, RequestMapping.class);

        if (Objects.isNull(classRequestMapping)) {
            return Optional.empty();
        }

        String beanPath = getPath(classRequestMapping);

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

    private List<MetaDataRegisterDTO> apiDefinition2ApiMeta(ApiBean<Object>.ApiDefinition apiDefinition) {

        ShenyuSpringMvcClient annotation = apiDefinition.getApiMethod().getAnnotation(ShenyuSpringMvcClient.class);

        return Lists.newArrayList(MetaDataRegisterDTO.builder()
                .contextPath(contextPath)
                .addPrefixed(addPrefixed)
                .appName(appName)
                .serviceName(apiDefinition.getBeanClass().getName())
                .methodName(Optional.ofNullable(apiDefinition.getApiMethod()).map(Method::getName).orElse(null))
                .path(apiDefinition.getApiPath())
                .pathDesc(annotation.desc())
                .parameterTypes(Optional.ofNullable(apiDefinition.getApiMethod())
                        .map(m -> Arrays.stream(m.getParameterTypes())
                                .map(Class::getName)
                                .collect(Collectors.joining(","))
                        ).orElse(null))
                .rpcType(RpcTypeEnum.HTTP.getName())
                .enabled(annotation.enabled())
                .ruleName(StringUtils.defaultIfBlank(annotation.ruleName(), apiDefinition.getApiPath()))
                .registerMetaData(annotation.registerMetaData())
                .build());
    }

    private List<ApiDocRegisterDTO> apiDefinition2ApiDoc(ApiBean<Object>.ApiDefinition apiDefinition) {

        ApiDoc apiDoc = apiDefinition.getApiMethod().getAnnotation(ApiDoc.class);
        List<String> tags = Arrays.asList(apiDoc.tags());
        String desc = apiDoc.desc();
        String apiPath = apiDefinition.getApiPath();

        RequestMapping requestMapping = AnnotatedElementUtils.findMergedAnnotation(apiDefinition.getApiMethod(), RequestMapping.class);

        String produce = requestMapping.produces().length == 0 ? ShenyuClientConstants.MEDIA_TYPE_ALL_VALUE : String.join(",", requestMapping.produces());
        String consume = requestMapping.consumes().length == 0 ? ShenyuClientConstants.MEDIA_TYPE_ALL_VALUE : String.join(",", requestMapping.consumes());
        RequestMethod[] requestMethods = requestMapping.method();
        if (requestMethods.length == 0) {
            requestMethods = RequestMethod.values();
        }
        List<ApiHttpMethodEnum> apiHttpMethodEnums = Stream.of(requestMethods).map(item -> ApiHttpMethodEnum.of(item.name())).collect(Collectors.toList());
        String version = "v0.01";
        List<ApiDocRegisterDTO> apiDocRegisters = new ArrayList<>();

        for (ApiHttpMethodEnum apiHttpMethodEnum : apiHttpMethodEnums) {
            ApiDocRegisterDTO build = ApiDocRegisterDTO.builder()
                    .consume(consume)
                    .produce(produce)
                    .httpMethod(apiHttpMethodEnum.getValue())
                    .contextPath(contextPath)
                    .ext("{}")
                    .document("{}")
                    .rpcType(RpcTypeEnum.HTTP.getName())
                    .version(version)
                    .apiDesc(desc)
                    .tags(tags)
                    .apiPath(apiPath)
                    .apiSource(ApiSourceEnum.ANNOTATION_GENERATION.getValue())
                    .state(ApiStateEnum.PUBLISHED.getState())
                    .apiOwner("admin")
                    .eventType(EventType.REGISTER)
                    .build();
            apiDocRegisters.add(build);
        }
        return apiDocRegisters;
    }
}
