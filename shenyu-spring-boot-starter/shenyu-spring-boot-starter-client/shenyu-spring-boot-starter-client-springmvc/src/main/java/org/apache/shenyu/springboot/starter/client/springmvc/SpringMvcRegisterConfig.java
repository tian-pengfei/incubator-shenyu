package org.apache.shenyu.springboot.starter.client.springmvc;

import org.apache.shenyu.client.apidocs.annotations.ApiDoc;
import org.apache.shenyu.client.apidocs.annotations.ApiModule;
import org.apache.shenyu.client.auto.config.ClientRegisterConfiguration;
import org.apache.shenyu.client.core.client.ApiBean;
import org.apache.shenyu.client.core.client.ClientInfoRefreshedEventListener;
import org.apache.shenyu.client.core.client.extractor.ApiBeansExtractor;
import org.apache.shenyu.client.core.client.matcher.AnnotatedApiBeanMatcher;
import org.apache.shenyu.client.core.client.matcher.AnnotatedApiDefinitionMatcher;
import org.apache.shenyu.client.core.client.matcher.Matcher;
import org.apache.shenyu.client.core.client.parser.ApiBeanMetaParser;
import org.apache.shenyu.client.core.client.parser.ApiDocParser;
import org.apache.shenyu.client.core.client.parser.ApiMetaParser;
import org.apache.shenyu.client.core.constant.ShenyuClientConstants;
import org.apache.shenyu.client.core.disruptor.ShenyuClientRegisterEventPublisher;
import org.apache.shenyu.client.springmvc.annotation.ShenyuSpringMvcClient;
import org.apache.shenyu.client.springmvc.register.SpringMvcApiBeanMetaParser;
import org.apache.shenyu.client.springmvc.register.SpringMvcApiBeansExtractor;
import org.apache.shenyu.client.springmvc.register.SpringMvcApiDocParser;
import org.apache.shenyu.client.springmvc.register.SpringMvcApiMetaParser;
import org.apache.shenyu.common.enums.RpcTypeEnum;
import org.apache.shenyu.common.utils.UriUtils;
import org.apache.shenyu.register.common.config.PropertiesConfig;
import org.apache.shenyu.register.common.config.ShenyuClientConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationUtils;
import java.util.Optional;
import java.util.Properties;

import static org.apache.shenyu.client.core.constant.ShenyuClientConstants.API_DOC_BEAN_MATCHER;
import static org.apache.shenyu.client.core.constant.ShenyuClientConstants.API_DOC_MATCHER;
import static org.apache.shenyu.client.core.constant.ShenyuClientConstants.API_META_BEAN_MATCHER;
import static org.apache.shenyu.client.core.constant.ShenyuClientConstants.API_META_BEAN_PRE_MATCHER;
import static org.apache.shenyu.client.core.constant.ShenyuClientConstants.API_META_MATCHER;

@Configuration(proxyBeanMethods = false)
@ConditionalOnBean(ClientRegisterConfiguration.class)
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
    @ConditionalOnMissingBean
    public ApiBeansExtractor<Object> apiBeansExtractor() {
        return new SpringMvcApiBeansExtractor(contextPath);
    }

    @Bean(name = API_META_BEAN_MATCHER)
    @ConditionalOnMissingBean(name = API_META_BEAN_MATCHER)
    public Matcher<ApiBean<Object>> apiMetaBeanMatcher() {
        return apiBean -> {
            ShenyuSpringMvcClient annotation = apiBean.getAnnotation(ShenyuSpringMvcClient.class);
            if (annotation != null) {
                return !annotation.path().contains("*");
            }
            return true;
        };
    }

    @Bean(name = API_META_BEAN_PRE_MATCHER)
    @ConditionalOnMissingBean(name = API_META_BEAN_PRE_MATCHER)
    public Matcher<ApiBean<Object>> apiMetaBeanPreMatcher() {

        return apiBean -> {
            ShenyuSpringMvcClient annotation = apiBean.getAnnotation(ShenyuSpringMvcClient.class);
            return annotation != null && annotation.path().contains("*");
        };
    }

    @Bean(name = API_META_MATCHER)
    @ConditionalOnMissingBean(name = API_META_MATCHER)
    public Matcher<ApiBean<Object>.ApiDefinition> apiMetaMatcher() {
        return new AnnotatedApiDefinitionMatcher<>(ShenyuSpringMvcClient.class)
                .or(api -> AnnotationUtils
                        .isAnnotationDeclaredLocally(ShenyuSpringMvcClient.class, api.getApiBean().getTargetClass()));
    }


    @Bean
    public ApiMetaParser<Object> apiMetaParser() {
        return new SpringMvcApiMetaParser(addPrefixed, appName);
    }

    @Bean(name = API_DOC_BEAN_MATCHER)
    @ConditionalOnMissingBean(name = API_DOC_BEAN_MATCHER)
    public Matcher<ApiBean<Object>> apiDocBeanMatcher() {
        return new AnnotatedApiBeanMatcher<>(ApiModule.class);
    }

    @Bean(name = API_DOC_MATCHER)
    @ConditionalOnMissingBean(name = API_DOC_MATCHER)
    public Matcher<ApiBean<Object>.ApiDefinition> apiDocMatcher() {
        return new AnnotatedApiDefinitionMatcher<>(ApiDoc.class);
    }

    @Bean
    @ConditionalOnMissingBean
    public ApiDocParser<Object> apiDocParser() {
        return new SpringMvcApiDocParser();
    }

    @Bean
    public ApiBeanMetaParser<Object> apiBeanMetaParser() {
        return new SpringMvcApiBeanMetaParser(addPrefixed, appName);
    }
}
