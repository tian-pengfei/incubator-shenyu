package org.apache.shenyu.client.core.client.registrar;

import org.apache.shenyu.client.core.client.ApiBean;
import org.apache.shenyu.client.core.client.matcher.Matcher;

import java.util.List;

public final class ApiBeanRegistrar<T> extends AbstractRegistrar<ApiBean<T>> {

    AbstractRegistrar<ApiBean<T>.ApiDefinition> apiRegistrar;

    public ApiBeanRegistrar(Matcher<ApiBean<T>> matcher,
                            AbstractRegistrar<ApiBean<T>.ApiDefinition> apiRegistrar) {
        super(matcher);
        this.apiRegistrar = apiRegistrar;
    }

    @Override
    protected void doRegister(ApiBean<T> element) {
        List<ApiBean<T>.ApiDefinition> apiDefinition = element.getApiDefinitions();
        apiDefinition.forEach(api -> apiRegistrar.register(api));
    }


}
