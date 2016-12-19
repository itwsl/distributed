/*
 * 文 件 名:  RedisDisLockTest.java
 * 版    权:  
 * 编写人:  51
 * 编 写 时 间:  2016年12月15日
 */
package com.mantu.common.distributed.lock;

import java.util.concurrent.TimeUnit;
import redis.clients.jedis.*;

/**
 * 
 * @author  51
 * @since  2016年12月15日
 */
public class RedisDisLockTest {

    
    public static void main(String [] args){
        RedisDisLockTest test = new RedisDisLockTest();
        //test.testOrder();
        //test.testOrder2();
        //test.testNOUnlock();
        test.testOtherUnlock();
    }
    public void testOrder(){
        JedisPool jp = new JedisPool("127.0.0.1",6379);
        
        for(int i=0;i<5;i++){
            Jedis jedis = jp.getResource();
            OrderThread th = new OrderThread("123456",jedis);
            th.start();
        }
    }
    public void testOrder2(){
        JedisPool jp = new JedisPool("127.0.0.1",6379);
        
        for(int i=0;i<5;i++){
            Jedis jedis = jp.getResource();
            OrderThread th = new OrderThread("1234567",jedis);
            th.start();
        }
    }
    public void testNOUnlock(){
        JedisPool jp = new JedisPool("127.0.0.1",6379);
        
        for(int i=0;i<5;i++){
            Jedis jedis = jp.getResource();
            TestNOUnlock th = new TestNOUnlock("12345678",jedis);
            th.start();
        }
    }
    public void testOtherUnlock(){
        JedisPool jp = new JedisPool("127.0.0.1",6379);
        
        for(int i=0;i<5;i++){
            Jedis jedis = jp.getResource();
            TestOtherUnlock th = new TestOtherUnlock("unlock",jedis);
            th.start();
        }
    }
    class OrderThread extends Thread{
        String lockKey="";
        Jedis jedis;
        public OrderThread(String lockKey,Jedis jedis){
            this.lockKey=lockKey;
            this.jedis=jedis;
        }
        public void run(){
            DisLock lock = LockSupport.getRedisLock(lockKey);
            try {
                if(lock.tryLock(2,TimeUnit.SECONDS,jedis)){
                    System.out.println("订单"+lockKey+"创建成功！");
                    lock.unlock(jedis);
                }
                else{
                    System.out.println("没有成功获取到锁");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
    class TestNOUnlock extends Thread{
        String lockKey="";
        Jedis jedis;
        public TestNOUnlock(String lockKey,Jedis jedis){
            this.lockKey=lockKey;
            this.jedis=jedis;
        }
        public void run(){
            DisLock lock = LockSupport.getRedisLock(lockKey);
            try {
                if(lock.tryLock(2,TimeUnit.SECONDS,jedis)){
                    System.out.println("订单"+lockKey+"创建成功！");
                    //lock.unlock(jedis);//no unlock
                }
                else{
                    System.out.println("没有成功获取到锁");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
    class TestOtherUnlock extends Thread{
        String lockKey="";
        Jedis jedis;
        public TestOtherUnlock(String lockKey,Jedis jedis){
            this.lockKey=lockKey;
            this.jedis=jedis;
        }
        public void run(){
            DisLock lock = LockSupport.getRedisLock(lockKey);
            if(lock.tryLock(jedis)){
                System.out.println("订单"+lockKey+"创建成功！");
                //lock.unlock(jedis);//no unlock
            }
            else{
                lock.unlock(jedis);
                System.out.println("TestOtherUnlock没有成功获取到锁");
            }
            
        }
    }
}
