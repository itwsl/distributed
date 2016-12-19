package com.mantu.common.distributed.lock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import redis.clients.jedis.Jedis;

public interface DisLock{
 
    boolean tryLock(Jedis jedis);
    boolean tryLock(long time, TimeUnit unit,Jedis jedis) throws InterruptedException;
    void unlock(Jedis jedis);
}