package com.shuitu.curator;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

/**
 * author:水菟丸
 */
public class CuratorCreateSessionDemo {
    private final static String CONNECTSTRING="192.168.11.129:2181,192.168.11.134:2181," +
            "192.168.11.135:2181,192.168.11.136:2181";
    public static void main(String[] args) {
        //创建会话的两种方式 normal
        CuratorFramework curatorFramework= CuratorFrameworkFactory.
                newClient(CONNECTSTRING,5000,5000,
                        new ExponentialBackoffRetry(1000,3));
        curatorFramework.start(); //start方法启动连接

        //fluent风格
        CuratorFramework curatorFramework1=CuratorFrameworkFactory.builder().connectString(CONNECTSTRING).sessionTimeoutMs(5000).
                retryPolicy(new ExponentialBackoffRetry(1000,3)).
                namespace("/curator").build();

        curatorFramework1.start();
        System.out.println("success");
    }
}
