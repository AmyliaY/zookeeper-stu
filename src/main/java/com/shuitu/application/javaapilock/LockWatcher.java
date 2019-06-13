package com.shuitu.application.javaapilock;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.util.concurrent.CountDownLatch;

/**
* @author 全恒
*/
public class LockWatcher implements Watcher{

    private CountDownLatch latch;

    public LockWatcher(CountDownLatch latch) {
        this.latch = latch;
    }

    public void process(WatchedEvent event) {
    	//如果节点被删除，则释放 闭锁
        if(event.getType() == Event.EventType.NodeDeleted){
            latch.countDown();
        }
    }
}
