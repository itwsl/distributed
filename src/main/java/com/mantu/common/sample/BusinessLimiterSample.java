package com.mantu.common.sample;

import java.util.Properties;

import com.mantu.common.util.BusinessLimiter;
import com.mantu.common.util.BusinessLimiterUtil;

public class BusinessLimiterSample {

	public static void main(String[] args) {
		Properties props = new Properties();
		props.setProperty("hospital1", "10");
		props.setProperty("hospital2", "10");
		props.setProperty("hospital3", "10");
		props.setProperty("default", "100");
		BusinessLimiterUtil.initProperties(props);
		for(int i=0;i<20;i++) {
			Runnable rn = new Runnable() {
			      public void run() {
			    	  BusinessLimiterSample bls = new BusinessLimiterSample();
					  bls.addOrder("hospital1", "张三", "李四"+Thread.currentThread().getId());
			      }
			    };
			Thread thread = new Thread(rn);
			thread.start();
		}
		for(int i=0;i<20;i++) {
			Runnable rn = new Runnable() {
			      public void run() {
			    	  BusinessLimiterSample bls = new BusinessLimiterSample();
					  bls.addOrder("hospital2", "张三", "李四"+Thread.currentThread().getId());
			      }
			    };
			Thread thread = new Thread(rn);
			thread.start();
		}
		for(int i=0;i<20;i++) {
			Runnable rn = new Runnable() {
			      public void run() {
			    	  BusinessLimiterSample bls = new BusinessLimiterSample();
					  bls.addOrder("hospital3", "张三", "李四"+Thread.currentThread().getId());
			      }
			    };
			Thread thread = new Thread(rn);
			thread.start();
		}
	}
	
	public void addOrder(String hosName,String doctorName,String userName) {
		BusinessLimiter limiter = BusinessLimiterUtil.getLimiter(hosName);
		if(limiter==null) {
			System.out.println("没有这个BusinessLimiter，请检查");
			return;
		}
		try{
			if(limiter.tryAcquire()) {
				System.out.println("恭喜你:"+userName+",已经挂到医院"+hosName+"医生"+doctorName+"的号");
				try {
					Thread.sleep(500L);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else {
				System.out.println("对不起，请重试！");
			}
		}
		finally {
			limiter.release();
		}
	}
}
