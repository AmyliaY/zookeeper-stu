package com.shuitu.curator;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

/**
* @author 全恒
*/
public class CuratorCreateSessionDemo {
	private final static String CONNECTSTRING = "192.168.123.38:2181,192.168.123.55:2181," +
            "192.168.123.45:2181,192.168.123.174:2181";
	
    public static void main(String[] args) {
        //创建会话的两种方式：
    	//1、normal
        CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient(CONNECTSTRING, 5000, 5000, 
                new ExponentialBackoffRetry(1000, 3));
        curatorFramework.start(); //用start方法启动连接

        //2、fluent风格
        CuratorFramework curatorFramework1 = CuratorFrameworkFactory
        		.builder()
        		.connectString(CONNECTSTRING)
        		.sessionTimeoutMs(5000)
        		.retryPolicy(new ExponentialBackoffRetry(1000, 3))
        		.namespace("/curator")//命名空间，表示创建连接后的所有节点操作都是在这个根节点下
        		.build();

        curatorFramework1.start();
        System.out.println("success");
    }
}