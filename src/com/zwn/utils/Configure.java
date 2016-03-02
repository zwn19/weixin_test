package com.zwn.utils;

import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

public class Configure{
	
	private static Properties pro = new Properties();
	
	private static Logger logger = Logger.getLogger(Configure.class);
	
	static{
		try {
			pro.load(Configure.class.getClassLoader().getResourceAsStream("app.properties"));
		} catch (IOException e) {
			logger.error("Fail to load app.properties",e);
		}
	}
	
	public static String get(String key){
		return pro.getProperty(key);
	}
	public static boolean getBoolean(String key){
		String val = pro.getProperty(key);
		return Boolean.getBoolean(val);
	}
}
