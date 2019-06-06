package com.shuitu.zkclient;

import org.I0Itec.zkclient.ZkClient;

/**
 * author:水菟丸
 */
public class SessionDemo {

	private final static String CONNECTSTRING = "192.168.123.38:2181,192.168.123.55:2181," +
            "192.168.123.45:2181,192.168.123.174:2181";

    public static void main(String[] args) {
        ZkClient zkClient = new ZkClient(CONNECTSTRING,4000);

        System.out.println(zkClient + " - > success");
    }
}
