package net.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.util.List;

public class JSONUtils {

    public static <T> T parse(String json, Class<T> clazz) {
        return JSON.parseObject(json, clazz);
    }

    public static <T> T parseObject(String json, TypeReference<T> ref) {
        return JSON.parseObject(json, ref, new Feature[0]);
    }

    public static String toJSONString(Object obj) {
        return JSON.toJSONString(obj);
    }

    public static String toJSONString(Object obj, SerializeFormater sf) {
        return JSON.toJSONString(obj, new SerializerFeature[]{sf.seria});
    }

    public static <T> List<T> parseArray(String json, Class<T> clazz) {
        return JSON.parseArray(json, clazz);
    }

    public static enum SerializeFormater {

        DEFAULT_DATE(SerializerFeature.WriteDateUseDateFormat);

        private SerializerFeature seria;

        private SerializeFormater(SerializerFeature seria) {
            this.seria = seria;
        }
    }
}
