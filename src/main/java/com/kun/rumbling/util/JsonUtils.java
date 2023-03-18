package com.kun.rumbling.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;

import java.io.IOException;
import java.util.List;

public class JsonUtils {

    private JsonUtils() {
    }

    private static final ObjectMapper objectMapper = new ObjectMapper();



    /**
     * JSON 字符串转换成指定类型
     *
     * @param json      JSON 字符串
     * @param valueType 转换后的类型
     * @param <T>       对象类型
     * @return 转换后的对象
     */
    public static <T> T readValue(String json, Class<T> valueType) {
        try {
            return objectMapper.readValue(json, valueType);
        } catch (IOException e) {
            throw new RuntimeException("json parse error");
        }
    }

    /**
     * JSON 字符串转换成指定类型的list集合
     *
     * @param json      JSON 字符串
     * @param valueType 转换后的类型
     * @param <T>       对象类型
     * @return 转换后的对象
     */
    public static <T> List<T> readArrayValue(String json, Class<T> valueType) {
        try {
            CollectionType collectionType = objectMapper.getTypeFactory().constructCollectionType(List.class, valueType);
            return objectMapper.readValue(json, collectionType);
        } catch (IOException e) {
            throw new RuntimeException("json parse error");
        }
    }


    /**
     * JSON 字符串转换成指定类型
     *
     * @param json         JSON 字符串
     * @param valueTypeRef 转换后的类型
     * @param <T>          对象类型
     * @return 转换后的对象
     */
    public static <T> T readValue(String json, TypeReference<T> valueTypeRef) {
        try {
            return objectMapper.readValue(json, valueTypeRef);
        } catch (IOException e) {
            throw new RuntimeException("json parse error");
        }
    }

    /**
     * 将对象转换成 JSON 字符串
     *
     * @param value 待转换的对象
     * @return JSON 字符串
     */
    public static String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (IOException e) {
            throw new RuntimeException("json parse error");
        }
    }

    /**
     * 将对象转换成指定类型
     *
     * @param obj   对象
     * @param clazz 目标类型
     * @param types 泛型类型
     * @return 目标对象
     */
    public static <T> T convertToType(Object obj, Class<T> clazz, Class<?>... types) {
        JavaType type = objectMapper.getTypeFactory().constructParametricType(clazz, types);
        try {
            return objectMapper.readValue(toJson(obj), type);
        } catch (IOException e) {
            throw new RuntimeException("json parse error");
        }
    }

}
