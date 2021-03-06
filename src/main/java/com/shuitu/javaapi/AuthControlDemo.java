package com.shuitu.javaapi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;

/**
* @author 全恒
*/
public class AuthControlDemo implements Watcher{
	
	private final static String CONNECTSTRING = "192.168.123.38:2181,192.168.123.55:2181," +
            "192.168.123.45:2181,192.168.123.174:2181";
	
    private static CountDownLatch countDownLatch = new CountDownLatch(1);

    private static ZooKeeper zookeeper;
    
    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
        zookeeper=new ZooKeeper(CONNECTSTRING, 5000, new AuthControlDemo());
        countDownLatch.await();

        //ACL （create/delete/write/read/admin）
        //权限模式： ip/Digest(username,pwd)/world对所有用户开放/super
        ACL acl = new ACL(ZooDefs.Perms.CREATE, new Id("digest","root:root"));
        ACL acl2 = new ACL(ZooDefs.Perms.CREATE, new Id("ip","192.168.1.1"));

        List<ACL> acls = new ArrayList<>();
        acls.add(acl);
        acls.add(acl2);
        zookeeper.create("/auth1","123".getBytes(),acls,CreateMode.PERSISTENT);
        zookeeper.addAuthInfo("digest","root:root".getBytes());
        zookeeper.create("/auth1","123".getBytes(), ZooDefs.Ids.CREATOR_ALL_ACL, CreateMode.PERSISTENT);
        zookeeper.create("/auth1/auth1-1","123".getBytes(), ZooDefs.Ids.CREATOR_ALL_ACL,CreateMode.EPHEMERAL);

        ZooKeeper zooKeeper1 = new ZooKeeper(CONNECTSTRING, 5000, new AuthControlDemo());
        countDownLatch.await();
        zooKeeper1.delete("/auth1",-1);

    }
    
    public void process(WatchedEvent watchedEvent) {
        //如果当前的连接状态是连接成功的，那么通过计数器去控制
        if(watchedEvent.getState() == Event.KeeperState.SyncConnected){
            if(Event.EventType.None == watchedEvent.getType() && null == watchedEvent.getPath()){
                countDownLatch.countDown();
                System.out.println(watchedEvent.getState()+"-->"+watchedEvent.getType());
            }
        }
    }
}
