/*
 * 文 件 名:  LockSupport.java
 * 版    权:  
 * 编写人:  51
 * 编 写 时 间:  2016年12月14日
 */
package com.mantu.common.distributed.lock;

import java.util.concurrent.ConcurrentHashMap;
import redis.clients.jedis.Jedis;


/**
 * 
 * @author  51
 * @since  2016年12月14日
 */
public class LockSupport {

    static ConcurrentHashMap <String,RedisDisLock>lockMap = new ConcurrentHashMap<String,RedisDisLock>();
    
    public static DisLock getRedisLock(String lockKey){
        RedisDisLock lock=null;
        if(lockMap.contains(lockKey)){
            lock = lockMap.get(lockKey);
        }
        else{
            RedisDisLock lockN = new RedisDisLock(lockKey);
            lock = lockMap.putIfAbsent(lockKey, lockN);
            if(lock==null){
                lock=lockN;
            }
        }
        return lock;
    }
}
