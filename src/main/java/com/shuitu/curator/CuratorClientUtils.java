package com.shuitu.curator;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

/**
 * author:水菟丸
 */
public class CuratorClientUtils {

    private static CuratorFramework curatorFramework;
    private final static String CONNECTSTRING="192.168.11.129:2181,192.168.11.134:2181," +
            "192.168.11.135:2181,192.168.11.136:2181";


    public static CuratorFramework getInstance(){
        curatorFramework= CuratorFrameworkFactory.
                newClient(CONNECTSTRING,5000,5000,
                        new ExponentialBackoffRetry(1000,3));
        curatorFramework.start();
        return curatorFramework;
    }
}
