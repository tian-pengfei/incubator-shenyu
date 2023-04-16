package org.apache.shenyu.client.core.client.parser;

import org.apache.shenyu.client.core.client.ApiBean;
import org.apache.shenyu.register.common.dto.MetaDataRegisterDTO;

public interface ApiBeanMetaParser<T> extends Parser<MetaDataRegisterDTO, ApiBean<T>> {

}
