package org.apache.shenyu.client.core.client.parser;

import org.apache.shenyu.client.core.client.ApiBean;
import org.apache.shenyu.register.common.dto.ApiDocRegisterDTO;

import java.util.List;

public interface ApiDocParser<T> extends Parser<List<ApiDocRegisterDTO>, ApiBean<T>.ApiDefinition> {

}
