package org.nvlp.tradeserver.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class JsonUtils {
    private final static ObjectMapper mapper = new ObjectMapper();

    public static <T> T parse(byte[] bytes, Class<T> clazz) {
        try {
            return mapper.readerFor(clazz).readValue(bytes);
        } catch (IOException e) {
            // i expect caller always consider this parse may return a null, which means input json is bad
            return null;
        }
    }
    public static <T> T parse(String json, Class<T> clazz) {
        try {
            return mapper.readerFor(clazz).readValue(json);
        } catch (JsonProcessingException e) {
            // i expect caller always consider this parse may return a null, which means input json is bad
            return null;
        }
    }

    public static String toString(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            // TODO: when do this happen?
            e.printStackTrace();
            return null;
        }
    }
}
