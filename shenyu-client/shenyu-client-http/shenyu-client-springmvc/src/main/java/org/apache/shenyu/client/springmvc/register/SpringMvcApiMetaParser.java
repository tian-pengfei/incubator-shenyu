package org.apache.shenyu.client.springmvc.register;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.shenyu.client.core.client.ApiBean;
import org.apache.shenyu.client.core.client.parser.ApiMetaParser;
import org.apache.shenyu.client.springmvc.annotation.ShenyuSpringMvcClient;
import org.apache.shenyu.common.enums.RpcTypeEnum;
import org.apache.shenyu.common.utils.PathUtils;
import org.apache.shenyu.register.common.dto.MetaDataRegisterDTO;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SpringMvcApiMetaParser implements ApiMetaParser<Object> {

    private final Boolean addPrefixed;

    private final String appName;

    public SpringMvcApiMetaParser(Boolean addPrefixed, String appName) {
        this.addPrefixed = addPrefixed;
        this.appName = appName;
    }

    @Override
    public List<MetaDataRegisterDTO> parse(ApiBean<Object>.ApiDefinition apiDefinition) {
        ShenyuSpringMvcClient annotation = apiDefinition.getAnnotation(ShenyuSpringMvcClient.class);

        annotation = annotation == null ? apiDefinition.getApiBean().getAnnotation(ShenyuSpringMvcClient.class)
                : annotation;
        String methodPath = annotation.path();
        if (StringUtils.isEmpty(methodPath)) {
            methodPath = apiDefinition.getMethodPath();
        }

        String apiPath = PathUtils.pathJoin(apiDefinition.getContextPath(), apiDefinition.getParentPath(), methodPath);

        String parameterTypes = (Optional.ofNullable(apiDefinition.getApiMethod())
                .map(m -> Arrays.stream(m.getParameterTypes()).map(Class::getName)
                        .collect(Collectors.joining(","))).orElse(null));

        return Lists.newArrayList(MetaDataRegisterDTO.builder()
                .contextPath(apiDefinition.getContextPath())
                .addPrefixed(addPrefixed)
                .appName(appName)
                .serviceName(apiDefinition.getBeanClass().getName())
                .methodName(apiDefinition.getApiMethodName())
                .path(apiPath)
                .pathDesc(annotation.desc())
                .parameterTypes(parameterTypes)
                .rpcType(RpcTypeEnum.HTTP.getName())
                .enabled(annotation.enabled())
                .ruleName(StringUtils.defaultIfBlank(annotation.ruleName(), apiDefinition.getApiPath()))
                .registerMetaData(annotation.registerMetaData())
                .build());
    }
}
