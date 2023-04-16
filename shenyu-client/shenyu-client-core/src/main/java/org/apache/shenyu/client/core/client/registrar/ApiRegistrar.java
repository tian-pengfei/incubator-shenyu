package org.apache.shenyu.client.core.client.registrar;

import org.apache.shenyu.client.core.client.ApiBean;
import org.apache.shenyu.client.core.client.matcher.Matcher;
import org.apache.shenyu.client.core.client.parser.Parser;
import org.apache.shenyu.client.core.disruptor.ShenyuClientRegisterEventPublisher;
import org.apache.shenyu.register.common.type.DataTypeParent;

import java.util.List;

public class ApiRegistrar<T, D extends DataTypeParent> extends AbstractRegistrar<ApiBean<T>.ApiDefinition> {

    private final ShenyuClientRegisterEventPublisher publisher;

    Parser<List<D>, ApiBean<T>.ApiDefinition> parser;

    public ApiRegistrar(Matcher<ApiBean<T>.ApiDefinition> matcher,
                        Parser<List<D>, ApiBean<T>.ApiDefinition> parser,
                        ShenyuClientRegisterEventPublisher publisher) {
        super(matcher);
        this.publisher = publisher;
        this.parser = parser;
    }

    @Override
    protected final void doRegister(ApiBean<T>.ApiDefinition element) {

        List<? extends D> datas = parser.parse(element);

        datas.forEach(publisher::publishEvent);
    }
}
