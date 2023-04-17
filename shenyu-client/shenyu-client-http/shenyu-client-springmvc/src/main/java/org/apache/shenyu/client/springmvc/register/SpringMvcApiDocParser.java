package org.apache.shenyu.client.springmvc.register;

import org.apache.shenyu.client.apidocs.annotations.ApiDoc;
import org.apache.shenyu.client.core.client.ApiBean;
import org.apache.shenyu.client.core.client.parser.ApiDocParser;
import org.apache.shenyu.client.core.constant.ShenyuClientConstants;
import org.apache.shenyu.common.enums.ApiHttpMethodEnum;
import org.apache.shenyu.common.enums.ApiSourceEnum;
import org.apache.shenyu.common.enums.ApiStateEnum;
import org.apache.shenyu.common.enums.RpcTypeEnum;
import org.apache.shenyu.register.common.dto.ApiDocRegisterDTO;
import org.apache.shenyu.register.common.enums.EventType;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SpringMvcApiDocParser implements ApiDocParser<Object> {

    @Override
    public List<ApiDocRegisterDTO> parse(ApiBean<Object>.ApiDefinition apiDefinition) {
        ApiDoc apiDoc = apiDefinition.getApiMethod().getAnnotation(ApiDoc.class);
        List<String> tags = Arrays.asList(apiDoc.tags());
        String desc = apiDoc.desc();
        String apiPath = apiDefinition.getApiPath();

        RequestMapping requestMapping = AnnotatedElementUtils.findMergedAnnotation(apiDefinition.getApiMethod(), RequestMapping.class);

        String produce = requestMapping.produces().length == 0 ? ShenyuClientConstants.MEDIA_TYPE_ALL_VALUE : String.join(",", requestMapping.produces());
        String consume = requestMapping.consumes().length == 0 ? ShenyuClientConstants.MEDIA_TYPE_ALL_VALUE : String.join(",", requestMapping.consumes());
        RequestMethod[] requestMethods = requestMapping.method();
        if (requestMethods.length == 0) {
            requestMethods = RequestMethod.values();
        }
        List<ApiHttpMethodEnum> apiHttpMethodEnums = Stream.of(requestMethods).map(item -> ApiHttpMethodEnum.of(item.name())).collect(Collectors.toList());
        String version = "v0.01";
        List<ApiDocRegisterDTO> apiDocRegisters = new ArrayList<>();

        for (ApiHttpMethodEnum apiHttpMethodEnum : apiHttpMethodEnums) {
            ApiDocRegisterDTO build = ApiDocRegisterDTO.builder()
                    .consume(consume)
                    .produce(produce)
                    .httpMethod(apiHttpMethodEnum.getValue())
                    .contextPath(apiDefinition.getContextPath())
                    .ext("{}")
                    .document("{}")
                    .rpcType(RpcTypeEnum.HTTP.getName())
                    .version(version)
                    .apiDesc(desc)
                    .tags(tags)
                    .apiPath(apiPath)
                    .apiSource(ApiSourceEnum.ANNOTATION_GENERATION.getValue())
                    .state(ApiStateEnum.PUBLISHED.getState())
                    .apiOwner("admin")
                    .eventType(EventType.REGISTER)
                    .build();
            apiDocRegisters.add(build);
        }
        return apiDocRegisters;
    }
}
