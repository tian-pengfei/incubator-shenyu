package org.apache.shenyu.client.core.client.extractor;

import org.apache.shenyu.client.core.client.ApiBean;
import org.springframework.context.ApplicationContext;

import java.util.List;

public interface ApiBeansExtractor<T> {

    List<ApiBean<T>> extract(ApplicationContext applicationContext);
}
