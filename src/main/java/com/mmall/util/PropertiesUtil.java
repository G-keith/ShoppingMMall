package com.mmall.util;


import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * @author GEMI
 * @date 2018/3/12/0012 15:56
 */
public class PropertiesUtil {

    private static Logger logger = LoggerFactory.getLogger(PropertiesUtil.class);

    private static Properties prop;

    static {
        String fileName = "mmall.properties";
        prop = new Properties();
        try {
            prop.load(new InputStreamReader(PropertiesUtil.class.getClassLoader().getResourceAsStream(fileName), "UTF-8"));
        } catch (IOException e) {
            logger.error("配置文件读取错误", e);
        }

    }

    public static String getValue(String key) {
        String value = prop.getProperty(key.trim());
        if (StringUtils.isBlank(value)) {
            return null;
        }
        return value.trim();
    }

    public static String getValue(String key,String defaultValue) {
        String value = prop.getProperty(key.trim());
        if (StringUtils.isBlank(value)) {
            value=defaultValue;
        }
        return value.trim();
    }
}
