package org.apache.shenyu.client.core.client;

import org.apache.shenyu.client.core.client.extractor.ApiBeansExtractor;
import org.apache.shenyu.client.core.client.registrar.ApiBeanRegistrar;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.List;

public final class ContextApiRefreshedEventListener<T> implements ApplicationListener<ContextRefreshedEvent> {

    private final List<ApiBeanRegistrar<T>> apiBeanRegistrars;

    private final ApiBeansExtractor<T> apiBeanExtractor;

    public ContextApiRefreshedEventListener(List<ApiBeanRegistrar<T>> apiBeanRegistrars, ApiBeansExtractor<T> apiBeanExtractor) {

        this.apiBeanExtractor = apiBeanExtractor;

        this.apiBeanRegistrars = apiBeanRegistrars;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {

        ApplicationContext applicationContext = event.getApplicationContext();

        List<ApiBean<T>> apiBeans = apiBeanExtractor.extract(applicationContext);

        apiBeans.forEach(apiBean ->
                apiBeanRegistrars.forEach(registrar -> registrar.register(apiBean))
        );
    }
}
