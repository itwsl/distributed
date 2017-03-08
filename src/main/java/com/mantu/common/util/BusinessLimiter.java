package com.mantu.common.util;

import java.util.HashMap;

public class BusinessLimiter {

	private int permits = 100;//最大同时使用此BusinessLimiter调用量(线程数)
	private int currentTickets = 0;//当前使用此BusinessLimiter的调用量(线程数)
	private HashMap<Thread,Integer> local = new HashMap<Thread,Integer>();
	public BusinessLimiter(int permits) {
		this.permits=permits;
	}
	
	public boolean tryAcquire() {
		synchronized (mutex()) {
			if(local.get(Thread.currentThread())!=null) {
				return true;
			}
			else if(currentTickets<permits){
				currentTickets++;
				local.put(Thread.currentThread(), 1);
				return true;
			}
		}
		return false;
	}
	
	public void release() {
		synchronized (mutex()) {
			if(local.get(Thread.currentThread())!=null) {
				currentTickets--;
				local.remove(Thread.currentThread());
			}
		}
	}
	
	private volatile Object mutexDoNotUseDirectly;
	private Object mutex() {
	    Object mutex = mutexDoNotUseDirectly;
	    if (mutex == null) {
	    	synchronized (this) {
	    		mutex = mutexDoNotUseDirectly;
	    		if (mutex == null) {
	    			mutexDoNotUseDirectly = mutex = new Object();
	    		}
	    	}
	    }
	    return mutex;
	}
	
}
