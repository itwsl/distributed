/*
 * 文 件 名:  CommonType.java
 * 版    权:  
 * 编写人:  51
 * 编 写 时 间:  2016年12月14日
 */
package com.mantu.common.distributed.lock;


/**
 * 
 * @author  51
 * @since  2016年12月14日
 */
public class CommonType {

    public static int WAITLOCKERS = 2;//当前服务器等待锁的线程数量，如果超过或等于此值，当前线程直接返回，不再等待锁
    public static String REDISKEY="mantu:dislock:";//redis下key前缀
    public static int LOCKEXPIRETIME = 5;//锁的过期时间，单位秒，默认5秒过期
}
