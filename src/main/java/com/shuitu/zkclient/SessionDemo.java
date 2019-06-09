package com.shuitu.zkclient;

import org.I0Itec.zkclient.ZkClient;

/**
* @author 全恒
*/
public class SessionDemo {

	private final static String CONNECTSTRING = "192.168.123.38:2181,192.168.123.55:2181," +
            "192.168.123.45:2181,192.168.123.174:2181";

    public static void main(String[] args) {
        ZkClient zkClient = new ZkClient(CONNECTSTRING, 4000);//4000表示连接超时 时间为4秒

        System.out.println(zkClient + " - > success");
    }
}
