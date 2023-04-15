package org.apache.shenyu.client.core.client;

import org.apache.shenyu.client.core.client.extractor.ApiBeansExtractor;
import org.apache.shenyu.client.core.client.matcher.ApiBeanMatcher;
import org.apache.shenyu.client.core.client.matcher.ApiDefinitionMatcher;
import org.apache.shenyu.client.core.client.parser.ApiDocParser;
import org.apache.shenyu.client.core.client.parser.ApiMetaParser;
import org.apache.shenyu.client.core.client.registrar.ApiBeanRegistrar;
import org.apache.shenyu.client.core.client.registrar.ApiRegistrar;
import org.apache.shenyu.client.core.disruptor.ShenyuClientRegisterEventPublisher;
import org.apache.shenyu.register.client.api.ShenyuClientRegisterRepository;
import org.apache.shenyu.register.common.dto.ApiDocRegisterDTO;
import org.apache.shenyu.register.common.dto.MetaDataRegisterDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static org.apache.shenyu.client.core.constant.ShenyuClientConstants.API_DOC_BEAN_MATCHER;
import static org.apache.shenyu.client.core.constant.ShenyuClientConstants.API_DOC_MATCHER;
import static org.apache.shenyu.client.core.constant.ShenyuClientConstants.API_META_BEAN_MATCHER;
import static org.apache.shenyu.client.core.constant.ShenyuClientConstants.API_META_MATCHER;

@Configuration(proxyBeanMethods = false)
public class ClientRegisterConfiguration {

//    @Bean
//    ClientInfoRefreshedEventListener clientInfoListener(PropertiesConfig clientConfig,ShenyuClientRegisterEventPublisher publisher, ){
//        return new ClientInfoRefreshedEventListener(clientConfig,publisher);
//    }

    @Bean
    <T> ContextApiRefreshedEventListener<T> apiListener(ApiBeansExtractor<T> apiBeanExtractor,List<ApiBeanRegistrar<T>> apiMetaBeanRegistrars){
        return new ContextApiRefreshedEventListener<>(apiMetaBeanRegistrars, apiBeanExtractor);
    }

    @Bean(name = "ApiMetaBeanRegistrar")
    @ConditionalOnProperty(value = "shenyu.register.api.meta.enabled", matchIfMissing = true, havingValue = "true")
    public<T> ApiBeanRegistrar<T> buildApiMetaBeanRegistrar(@Qualifier(API_META_BEAN_MATCHER) ApiBeanMatcher<T> apiMetaBeanMatcher,
                                                            @Qualifier(API_META_MATCHER)ApiDefinitionMatcher<T> apiMetaMatcher,
                                                           ApiMetaParser<T> apiMetaParser,
                                                           ShenyuClientRegisterEventPublisher publisher) {

        ApiRegistrar<T, MetaDataRegisterDTO> apiDocRegistrar =
                new ApiRegistrar<>(apiMetaMatcher, apiMetaParser, publisher);
        return new ApiBeanRegistrar<>(apiMetaBeanMatcher, apiDocRegistrar);
    }

    @Bean(name = "ApiDocBeanRegistrar")
    @ConditionalOnProperty(value = "shenyu.register.api.doc.enabled", matchIfMissing = true, havingValue = "true")
    public<T> ApiBeanRegistrar<T> buildApiDocBeanRegistrar(@Qualifier(API_DOC_BEAN_MATCHER)ApiBeanMatcher<T> apiDocBeanMatcher,
                                                           @Qualifier(API_DOC_MATCHER)ApiDefinitionMatcher<T> apiDocMatcher,
                                                           ApiDocParser<T> apiDocParser,
                                                           ShenyuClientRegisterEventPublisher publisher) {

        ApiRegistrar<T, ApiDocRegisterDTO> apiDocRegistrar =
                new ApiRegistrar<>(apiDocMatcher, apiDocParser, publisher);
        return new ApiBeanRegistrar<>(apiDocBeanMatcher, apiDocRegistrar);
    }

    @Bean
    public ShenyuClientRegisterEventPublisher publisher(final ShenyuClientRegisterRepository shenyuClientRegisterRepository) {
        ShenyuClientRegisterEventPublisher publisher = ShenyuClientRegisterEventPublisher.getInstance();
        publisher.start(shenyuClientRegisterRepository);
        return publisher;
    }
}
