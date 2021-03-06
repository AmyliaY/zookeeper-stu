package com.shuitu.zkclient;

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
* @author 全恒
*/
public class ZkClientApiOperatorDemo {

	private final static String CONNECTSTRING = "192.168.123.38:2181,192.168.123.55:2181," +
            "192.168.123.45:2181,192.168.123.174:2181";

    private static ZkClient getInstance(){
        return new ZkClient(CONNECTSTRING, 10000);//建立连接，允许超时10秒
    }

    public static void main(String[] args) throws InterruptedException {
        ZkClient zkClient=getInstance();
        //zkclient 提供了递归创建节点的功能
        zkClient.createPersistent("/zkclient/zkclient1/zkclient1-1/zkclient1-1-1",true);
        System.out.println("success");

        //删除节点
        zkClient.delete("/shuitu");
        //递归删除节点
        zkClient.deleteRecursive("/zkclient");

        //获取子节点
        List<String> list = zkClient.getChildren("/node");
        System.out.println(list);

        //注册watcher 监听"/node"节点是否被修改或删除
        zkClient.subscribeDataChanges("/node", new IZkDataListener() {
            @Override
            public void handleDataChange(String s, Object o) throws Exception {
                System.out.println("节点名称：" + s + "->节点修改后的值" + o);
            }

            @Override
            public void handleDataDeleted(String s) throws Exception {

            }
        });
        //修改"/node"节点的值，测试监听是否正常启用
        zkClient.writeData("/node", "node");
        TimeUnit.SECONDS.sleep(2);

        //注册watcher 监听"/node"节点的子节点
        zkClient.subscribeChildChanges("/node", new IZkChildListener() {
            @Override
            public void handleChildChange(String s, List<String> list) throws Exception {

            }
        });
    }
}
