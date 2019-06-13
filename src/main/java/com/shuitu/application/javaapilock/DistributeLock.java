package com.shuitu.application.javaapilock;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.junit.Test;

/**
* @author 全恒
* 用zookeeper的Java API实现分布式锁
*/
public class DistributeLock {

    private static final String ROOT_LOCKS = "/LOCKS";//根节点

    private ZooKeeper zooKeeper;

    private int sessionTimeout; //会话超时时间

    private String lockID; //记录锁节点id

    private final static byte[] data = {1, 2}; //节点的数据

    private CountDownLatch countDownLatch = new CountDownLatch(1);

    public DistributeLock() throws IOException, InterruptedException {
        this.zooKeeper = ZookeeperClient.getInstance();
        this.sessionTimeout = ZookeeperClient.getSessionTimeout();
    }

    //获取锁的方法
    public boolean lock(){
        try {
            //LOCKS/00000001
            lockID = zooKeeper.create(ROOT_LOCKS+"/", data, ZooDefs.Ids.
                    OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);

            System.out.println(Thread.currentThread().getName() + "->成功创建了lock节点[" + lockID + "],开始去竞争锁");

            //获取根节点下的所有子节点
            List<String> childrenNodes = zooKeeper.getChildren(ROOT_LOCKS, true);
            //排序，从小到大
            SortedSet<String> sortedSet = new TreeSet<String>();
            for(String children : childrenNodes){
                sortedSet.add(ROOT_LOCKS + "/" + children);
            }
            String first = sortedSet.first(); //拿到最小的节点
            if(lockID.equals(first)){
                //表示当前就是最小的节点
                System.out.println(Thread.currentThread().getName() + "->成功获得锁，lock节点为:[" + lockID + "]");
                return true;
            }
            //获取所有比当前lockID小的节点集合
            SortedSet<String> lessThanLockId = sortedSet.headSet(lockID);
            if(!lessThanLockId.isEmpty()){
            	//拿到比当前lockID小的上一节点
                String prevLockID = lessThanLockId.last();
                //监听前一个节点是否被释放，然后设置监听的过期时间，如果监听超时则放弃获取锁，若节点被释放则立刻去获取锁
                zooKeeper.exists(prevLockID, new LockWatcher(countDownLatch));
                boolean flag = countDownLatch.await(sessionTimeout, TimeUnit.MILLISECONDS);
                if(!flag)
                	return false;
                System.out.println(Thread.currentThread().getName() + " 成功获取锁：[" + lockID + "]");
            }
            return true;
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean unlock(){
        System.out.println(Thread.currentThread().getName() + "->开始释放锁:[" + lockID + "]");
        try {
            zooKeeper.delete(lockID, -1);
            System.out.println("节点[" + lockID + "]成功被删除");
            return true;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void main(String[] args) {
        final CountDownLatch latch = new CountDownLatch(10);
        Random random = new Random();
        for(int i=0; i<10; i++){
            new Thread(()->{
                DistributeLock lock = null;
                try {
                    lock = new DistributeLock();
                    //使用闭锁，当10个线程都countDown()后，释放闭锁，使10个线程同时去争夺锁
                    latch.countDown();
                    latch.await();
                    lock.lock();
                    Thread.sleep(random.nextInt(500));
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }finally {
                    if(lock != null){
                        lock.unlock();
                    }
                }
            }).start();
        }
    }
}
