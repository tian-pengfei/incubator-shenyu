package org.apache.shenyu.client.core.client.registrar;

import org.apache.shenyu.client.core.client.ApiBean;
import org.apache.shenyu.client.core.client.matcher.Matcher;
import org.apache.shenyu.client.core.client.parser.Parser;
import org.apache.shenyu.client.core.disruptor.ShenyuClientRegisterEventPublisher;
import org.apache.shenyu.register.common.type.DataTypeParent;

public class ApiBeanPreRegistrar<T, D extends DataTypeParent> extends AbstractRegistrar<ApiBean<T>> {

    private final ShenyuClientRegisterEventPublisher publisher;

    Parser<? extends D, ApiBean<T>> parser;

    public ApiBeanPreRegistrar(Matcher<ApiBean<T>> matcher,
                               Parser<? extends D, ApiBean<T>> parser,
                               ShenyuClientRegisterEventPublisher publisher) {
        super(matcher);
        this.parser = parser;
        this.publisher = publisher;
    }

    @Override
    protected void doRegister(ApiBean<T> element) {
        D d = parser.parse(element);
        publisher.publishEvent(d);
    }
}
