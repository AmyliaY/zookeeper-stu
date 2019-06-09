package com.shuitu.javaapi;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

/**
* @author 全恒
*/
public class Practice implements Watcher{

	private final static String CONNECTSTRING = "192.168.123.38:2181,192.168.123.55:2181," +
            "192.168.123.45:2181,192.168.123.174:2181";
	
	private static CountDownLatch countDownLatch = new CountDownLatch(1);
	
	private static Stat stat = new Stat();
	
	private static ZooKeeper zooKeeper;
	
	public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
		zooKeeper = new ZooKeeper(CONNECTSTRING, 5000, new Practice());//new Practice()相当于注册的监听器
		countDownLatch.await();
		System.out.println("zookeeper客户端的连接状态：" + zooKeeper.getState());
		
		//创建临时节点CreateMode.EPHEMERAL
		String result = zooKeeper.create("/shuitu", "666".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
		zooKeeper.getData(result, true, stat);//继续watcher监听
		System.out.println("创建成功：" + result);
		
		//修改节点
		zooKeeper.setData(result, "shuitu233".getBytes(), -1);
		Thread.sleep(2000);//zookeeper的watcher是异步的
		zooKeeper.setData(result, "shuitu333".getBytes(), -1);
		Thread.sleep(2000);
		
		//删除节点
		zooKeeper.delete(result, -1);
		Thread.sleep(2000);
		
		//创建节点和子节点，然后修改子节点（临时节点下面不能挂子节点）
		String path = "/persistentNode";
		zooKeeper.create(path, "321".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		TimeUnit.SECONDS.sleep(1);//睡一秒
		zooKeeper.create(path + "/child", "IJustAChild".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		TimeUnit.SECONDS.sleep(1);
		
		zooKeeper.setData(path + "/child", "123".getBytes(), -1);
		TimeUnit.SECONDS.sleep(1);
		
		//判断所要创建的节点是否存在，不存在时才进行创建，防止NodeExists异常
		String path2 = "ManOfCloud";
		Stat stat2 = zooKeeper.exists(path2, true);
		if(stat2 == null){
			zooKeeper.create(path2, "云芷君".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			TimeUnit.SECONDS.sleep(1);
		}
		
		//获取指定节点的所有子节点
		List<String> list = zooKeeper.getChildren(path, true);
		System.out.println(list);
		
		
	}

	@Override
	public void process(WatchedEvent watchedEvent) {
		//如果当前的连接状态是成功的，那么通过countDownLatch.countDown()去控制闭锁countDownLatch.await()打开
		if(watchedEvent.getState() == Event.KeeperState.SyncConnected){
			//watcher最开始是没有监听任何节点的，直到有节点注册了这个监听器
			if(Event.EventType.None == watchedEvent.getType() && null == watchedEvent.getPath()){
				countDownLatch.countDown();
				System.out.println("watcher监听事件的状态:" + watchedEvent.getState() + "-->watcher监听事件的类型:" + watchedEvent.getType());
			}
			else if (watchedEvent.getType() == Event.EventType.NodeDataChanged){//修改节点会触发
				try {
					System.out.println("所修改节点的路径：" + watchedEvent.getPath() + 
							"--> 改变后的值：" + zooKeeper.getData(watchedEvent.getPath(), true, stat));//true表示继续监听，false表示只监听一次，
																										//后面的都不会再进行监听
				} catch (KeeperException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if (watchedEvent.getType() == Event.EventType.NodeChildrenChanged){//子节点被修改会触发
				try {
					System.out.println("修改了子节点的节点路径：" + watchedEvent.getPath() + 
							"--> 改变后的值：" + zooKeeper.getData(watchedEvent.getPath(), true, stat));//true表示继续监听，false表示只监听一次，
																										//后面的都不会再进行监听
				} catch (KeeperException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if (watchedEvent.getType() == Event.EventType.NodeCreated){//子节点被创建会触发
				try {
					System.out.println("所创建节点的路径" + watchedEvent.getPath() + 
							"--> 节点的值" + zooKeeper.getData(watchedEvent.getPath(), true, stat));
				} catch (KeeperException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if (watchedEvent.getType() == Event.EventType.NodeDeleted){//子节点删除会触发
				try {
					System.out.println("所删除节点的路径" + watchedEvent.getPath() + 
							"--> 节点的值" + zooKeeper.getData(watchedEvent.getPath(), true, stat));
				} catch (KeeperException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}
}
