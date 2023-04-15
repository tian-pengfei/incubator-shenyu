package org.apache.shenyu.client.core.client.parser;

import org.apache.shenyu.client.core.client.ApiBean;
import org.apache.shenyu.register.common.dto.MetaDataRegisterDTO;

import java.util.List;

public interface ApiMetaParser<T> extends Parser<List<MetaDataRegisterDTO>, ApiBean<T>.ApiDefinition> {

}
