/*
 * 文 件 名:  RedisDisLock.java
 * 版    权:  
 * 编写人:  51
 * 编 写 时 间:  2016年12月14日
 */
package com.mantu.common.distributed.lock;

import java.util.concurrent.TimeUnit;
import redis.clients.jedis.Jedis;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author  51
 * @since  2016年12月14日
 */
public class RedisDisLock implements DisLock{
    private static final Logger LOG = LoggerFactory.getLogger(RedisDisLock.class);
    private transient Thread exclusiveOwnerThread;
    
    String lockKey="";
    AtomicInteger waitToLock=new AtomicInteger(0);
    
    public RedisDisLock(String lockKey){
        this.lockKey=CommonType.REDISKEY+lockKey;
    }

    public boolean tryLock(Jedis jedis) {
        Thread thread = Thread.currentThread();
        if(thread==this.getExclusiveOwnerThread()){
            return true;
        }
        Long i = jedis.setnx(lockKey, System.currentTimeMillis()+"");
        if(i.intValue()==1){
            jedis.expire(lockKey, CommonType.LOCKEXPIRETIME);
            setExclusiveOwnerThread(thread);
            return true;
        }
        else{//对于可能性非常低的死锁情况进行解锁
            String initTime = jedis.get(lockKey);
            if(initTime==null){
                LOG.debug("initTime's value is null");
                return false;
            }
            long iniTime=0L;
            try{
                iniTime = Long.parseLong(initTime);
            }
            catch(NumberFormatException nfex){
                LOG.warn(nfex.getMessage());
                jedis.expire(lockKey, 1);
                return false;
            }
            if(((System.currentTimeMillis()-iniTime)/1000-CommonType.LOCKEXPIRETIME-1)>0){
                String oldTime = jedis.getSet(lockKey, System.currentTimeMillis()+"");//对于及其极端的情况，lock被线程1处理掉了，但是又被线程2getset新的值了，通过下一次调用trylock()方法处理
                if(oldTime==null){
                    LOG.info("oldTime is null");
                    return false;
                }
                if(initTime.equals(oldTime)){
                    release(jedis);
                }
            }
        }
        return false;
    }

    public boolean tryLock(long timeout, TimeUnit unit,Jedis jedis) throws InterruptedException {
        long nanosTimeout = unit.toNanos(timeout);
        long lastTime = System.nanoTime();
        if(tryLock(jedis)){
            return true;
        }
        try{
            int waitLockers = waitToLock.getAndIncrement();
            if(waitLockers>=CommonType.WAITLOCKERS){
                LOG.debug("wait the lock' thread num is much,so return flase");
                return false;
            }
            for(;;){
                if(tryLock(jedis)){
                    return true;
                }
                if (nanosTimeout <= 0){
                    LOG.debug("getlock timeout");
                    return false;
                }
                if(nanosTimeout>100000){
                    LockSupport.parkNanos(100000);//中断100毫秒
                }
                long now = System.nanoTime();
                nanosTimeout -= now - lastTime;
                lastTime = now;
                if (nanosTimeout <= 0){
                    LOG.debug("getlock timeout");
                    return false;
                }
                if (Thread.interrupted()){
                    throw new InterruptedException();
                }
            }
        }
        finally{
            waitToLock.decrementAndGet();
        }
    }

    public void unlock(Jedis jedis) {
        Thread thread = Thread.currentThread();
        if(thread==this.getExclusiveOwnerThread()){
            LOG.debug("unlock the thread {}",thread.getId());
            release(jedis);
        }
    }
    
    private void release(Jedis jedis){
        setExclusiveOwnerThread(null);
        jedis.del(lockKey);
    }
    
    /**
     * Sets the thread that currently owns exclusive access. A
     * <tt>null</tt> argument indicates that no thread owns access.
     * This method does not otherwise impose any synchronization or
     * <tt>volatile</tt> field accesses.
     */
    protected final void setExclusiveOwnerThread(Thread t) {
        exclusiveOwnerThread = t;
    }

    /**
     * Returns the thread last set by
     * <tt>setExclusiveOwnerThread</tt>, or <tt>null</tt> if never
     * set.  This method does not otherwise impose any synchronization
     * or <tt>volatile</tt> field accesses.
     * @return the owner thread
     */
    protected final Thread getExclusiveOwnerThread() {
        return exclusiveOwnerThread;
    }
}
