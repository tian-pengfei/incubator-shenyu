package org.apache.shenyu.client.core.client;

import com.google.common.collect.Lists;
import org.apache.shenyu.client.core.client.extractor.ApiBeansExtractor;
import org.apache.shenyu.client.core.client.matcher.Matcher;
import org.apache.shenyu.client.core.client.parser.Parser;
import org.apache.shenyu.client.core.client.registrar.ApiBeanRegistrar;
import org.apache.shenyu.client.core.client.registrar.ApiRegistrar;
import org.apache.shenyu.client.core.disruptor.ShenyuClientRegisterEventPublisher;
import org.apache.shenyu.register.common.dto.ApiDocRegisterDTO;
import org.apache.shenyu.register.common.dto.MetaDataRegisterDTO;

import java.util.Collections;
import java.util.List;


public class ApiRegisterDocket<T> {

    private final ApiBeansExtractor<T> apiBeansExtractor;

    private Matcher<ApiBean<T>> apiDocBeanMatcher = e -> true;

    private Matcher<ApiBean<T>> apiMetaBeanMatcher = e -> true;

    private Matcher<ApiBean<T>.ApiDefinition> apiDocMatcher = e -> true;

    private Matcher<ApiBean<T>.ApiDefinition> apiMetaMatcher = e -> true;

    private Parser<List<ApiDocRegisterDTO>, ApiBean<T>.ApiDefinition> apiDocParser = e -> Collections.emptyList();

    private Parser<List<MetaDataRegisterDTO>, ApiBean<T>.ApiDefinition> apiMetaParser = e -> Collections.emptyList();

    private final ShenyuClientRegisterEventPublisher publisher;

    public ApiRegisterDocket(ApiBeansExtractor<T> apiBeansExtractor, final ShenyuClientRegisterEventPublisher publisher) {

        this.apiBeansExtractor = apiBeansExtractor;

        this.publisher = publisher;
    }

    public ApiRegisterDocket<T> apiDocBeanMatcher(Matcher<ApiBean<T>> apiDocBeanMatcher) {
        this.apiDocBeanMatcher = apiDocBeanMatcher;
        return this;
    }

    public ApiRegisterDocket<T> apiDocMatcher(Matcher<ApiBean<T>.ApiDefinition> apiDocMatcher) {
        this.apiDocMatcher = apiDocMatcher;
        return this;
    }

    public ApiRegisterDocket<T> apiDocParser(Parser<List<ApiDocRegisterDTO>, ApiBean<T>.ApiDefinition> apiDocParser) {
        this.apiDocParser = apiDocParser;
        return this;
    }

    public ApiRegisterDocket<T> apiMetaParser(Parser<List<MetaDataRegisterDTO>, ApiBean<T>.ApiDefinition> apiMetaParser) {
        this.apiMetaParser = apiMetaParser;
        return this;
    }

    public ApiRegisterDocket<T> apiMetaBeanMatcher(Matcher<ApiBean<T>> apiMetaBeanMatcher) {
        this.apiMetaBeanMatcher = apiMetaBeanMatcher;
        return this;
    }

    public ApiRegisterDocket<T> apiMetaMatcher(Matcher<ApiBean<T>.ApiDefinition> apiMetaMatcher) {
        this.apiMetaMatcher = apiMetaMatcher;
        return this;
    }

    public ContextApiRefreshedEventListener<T> buildApiRefreshedEventListener() {
        return new ContextApiRefreshedEventListener<>(
                Lists.newArrayList(buildApiDocBeanRegistrar(), buildApiMetaBeanRegistrar()),
                apiBeansExtractor);
    }

    private ApiBeanRegistrar<T> buildApiDocBeanRegistrar() {

        ApiRegistrar<T, ApiDocRegisterDTO> apiDocRegistrar =
                new ApiRegistrar<>(apiDocMatcher, apiDocParser, publisher);

        return new ApiBeanRegistrar<>(apiDocBeanMatcher, apiDocRegistrar);
    }

    private ApiBeanRegistrar<T> buildApiMetaBeanRegistrar() {

        ApiRegistrar<T, MetaDataRegisterDTO> apiMetaRegistrar =
                new ApiRegistrar<>(apiMetaMatcher, apiMetaParser, publisher);

        return new ApiBeanRegistrar<>(apiMetaBeanMatcher, apiMetaRegistrar);
    }
}
