package org.openflamingo.web.core;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

public class ObjectMapeprFactoryBean implements FactoryBean<ObjectMapper>, InitializingBean {

    private org.codehaus.jackson.map.ObjectMapper objectMapper;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(SerializationConfig.Feature.WRITE_NULL_MAP_VALUES, true);
        // TODO: Jackson Version conflict problem
//        this.objectMapper.configure(SerializationConfig.Feature.WRITE_EMPTY_JSON_ARRAYS, false);
//        this.objectMapper.configure(DeserializationConfig.Feature.UNWRAP_ROOT_VALUE, true);
//        this.objectMapper.configure(DeserializationConfig.Feature.USE_JAVA_ARRAY_FOR_JSON_ARRAY, true);
        this.objectMapper.configure(DeserializationConfig.Feature.USE_ANNOTATIONS, true);
        this.objectMapper.configure(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        this.objectMapper.configure(DeserializationConfig.Feature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        this.objectMapper.configure(DeserializationConfig.Feature.READ_ENUMS_USING_TO_STRING, true);
        this.objectMapper.configure(DeserializationConfig.Feature.READ_ENUMS_USING_TO_STRING, true);
        this.objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.objectMapper.getSerializationConfig().setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
    }

    @Override
    public ObjectMapper getObject() throws Exception {
        return objectMapper;
    }

    @Override
    public Class<?> getObjectType() {
        return ObjectMapper.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
