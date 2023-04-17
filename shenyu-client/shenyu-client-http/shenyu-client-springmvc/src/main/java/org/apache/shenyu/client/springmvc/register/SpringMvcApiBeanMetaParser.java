package org.apache.shenyu.client.springmvc.register;

import org.apache.commons.lang3.StringUtils;
import org.apache.shenyu.client.core.client.ApiBean;
import org.apache.shenyu.client.core.client.parser.ApiBeanMetaParser;
import org.apache.shenyu.client.springmvc.annotation.ShenyuSpringMvcClient;
import org.apache.shenyu.common.enums.RpcTypeEnum;
import org.apache.shenyu.common.utils.PathUtils;
import org.apache.shenyu.register.common.dto.MetaDataRegisterDTO;

public class SpringMvcApiBeanMetaParser implements ApiBeanMetaParser<Object> {

    private final Boolean addPrefixed;

    private final String appName;

    public SpringMvcApiBeanMetaParser(Boolean addPrefixed, String appName) {
        this.addPrefixed = addPrefixed;
        this.appName = appName;
    }

    @Override
    public MetaDataRegisterDTO parse(ApiBean<Object> apiBean) {
        return apiBean2ApiMeta(apiBean);
    }

    private MetaDataRegisterDTO apiBean2ApiMeta(ApiBean<Object> apiBean) {

        ShenyuSpringMvcClient annotation = apiBean.getAnnotation(ShenyuSpringMvcClient.class);
        String apiPath = PathUtils.pathJoin(apiBean.getContextPath(), annotation.path());

        return MetaDataRegisterDTO.builder()
                .contextPath(apiBean.getContextPath())
                .addPrefixed(addPrefixed)
                .appName(appName)
                .serviceName(apiBean.getTargetClass().getName())
                .methodName(null)
                .path(apiPath)
                .pathDesc(annotation.desc())
                .parameterTypes(null)
                .rpcType(RpcTypeEnum.HTTP.getName())
                .enabled(annotation.enabled())
                .ruleName(StringUtils.defaultIfBlank(annotation.ruleName(), apiBean.getBeanPath()))
                .registerMetaData(annotation.registerMetaData())
                .build();
    }

}
