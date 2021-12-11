package top.wdcc.freeswitch.common.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;

public class JsonUtils {
    public static Map<String, String> string2Map(String json){
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String,String>>(){});
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> Map<String, String> object2Map(T o){
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String json = objectMapper.writeValueAsString(o);
            return objectMapper.readValue(json, new TypeReference<Map<String,String>>(){});
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> T string2Object(String json, Class<T> o){
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(json, o);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}

