package com.shuitu.javaapi;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.Watcher.Event;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
* @author 全恒
*/
public class CreateSessionDemo {
	
	private final static String CONNECTSTRING = "192.168.123.38:2181,192.168.123.55:2181," +
            "192.168.123.45:2181,192.168.123.174:2181";
	
	private static CountDownLatch countDownLatch = new CountDownLatch(1);
	
	public static void main(String[] args) throws IOException, InterruptedException {
		ZooKeeper zooKeeper = new ZooKeeper(CONNECTSTRING, 5000, new Watcher() {
			
			@Override
			public void process(WatchedEvent event) {
				//如果当前的连接状态是成功的，那么通过
				if(event.getState() == Event.KeeperState.SyncConnected){
					countDownLatch.countDown();
					System.out.println("WatchedEvent的状态：" + event.getState());
				}
			}
		});
		countDownLatch.await();
		System.out.println("zookeeper客户端的连接状态：" + zooKeeper.getState());
	}
}
