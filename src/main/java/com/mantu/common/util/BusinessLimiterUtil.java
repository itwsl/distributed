package com.mantu.common.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Iterator;
import java.util.Properties;

import com.mantu.common.util.BusinessLimiter;

public class BusinessLimiterUtil {
	private static ConcurrentHashMap<String,BusinessLimiter> limiterMap = new ConcurrentHashMap<String,BusinessLimiter>();
	public static BusinessLimiter createLimiter(String limiterKey,int permits) {
		if(!limiterMap.containsKey(limiterKey)) {
			BusinessLimiter limiter = new BusinessLimiter(permits);
			limiterMap.putIfAbsent(limiterKey, limiter);
			return limiter;
		}
		else {
			return limiterMap.get(limiterKey);
		}
	}
	public static BusinessLimiter getLimiter(String limiterKey) {
		if(limiterMap.containsKey(limiterKey)) {
			return limiterMap.get(limiterKey);
		}
		else if(limiterMap.containsKey("default")){
			return limiterMap.get("default");
		}
		return null;
	}
	public static void initProperties(Properties prop) {
		Iterator<String> it=prop.stringPropertyNames().iterator();
        while(it.hasNext()){
            String key=it.next();
            createLimiter(key,Integer.parseInt(prop.getProperty(key).trim()));
        }
	}
}
