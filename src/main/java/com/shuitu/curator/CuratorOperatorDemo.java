package com.shuitu.curator;

import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.api.transaction.CuratorTransactionResult;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

/**
* @author 全恒
*/
public class CuratorOperatorDemo {

    public static void main(String[] args) throws InterruptedException {
        CuratorFramework curatorFramework = CuratorClientUtils.getInstance();
        System.out.println("连接成功.........");

        //创建节点
        try {
            String result = curatorFramework
            		.create()
            		.creatingParentsIfNeeded()
            		.withMode(CreateMode.PERSISTENT)
            		.forPath("/curator/curator1/curator11", "123".getBytes());
            System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //删除节点
        try {
            //默认情况下，version为-1
            curatorFramework.delete().deletingChildrenIfNeeded().forPath("/node11");
        } catch (Exception e) {
            e.printStackTrace();
        }

        //查询
        Stat stat = new Stat();
        try {
            byte[] bytes = curatorFramework.getData().storingStatIn(stat).forPath("/curator");
            System.out.println(new String(bytes) + "-->stat:" + stat);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //修改
        try {
            Stat stat1 = curatorFramework.setData().forPath("/curator", "123".getBytes());
            System.out.println(stat1);
        } catch (Exception e) {
            e.printStackTrace();
        }


        //通过线程池来实现异步操作
        ExecutorService service = Executors.newFixedThreadPool(1);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        try {
            curatorFramework
            	.create()
            	.creatingParentsIfNeeded()
            	.withMode(CreateMode.EPHEMERAL)
            	.inBackground(new BackgroundCallback() {//节点创建完成后回调本方法
                    @Override
                    public void processResult(CuratorFramework curatorFramework, CuratorEvent curatorEvent) throws Exception {
                        System.out.println(Thread.currentThread().getName() + "->resultCode:" + curatorEvent.getResultCode() + "->"
                        		+ curatorEvent.getType());
                        countDownLatch.countDown();
                    }
                },service)
            	.forPath("/mic", "123".getBytes());//创建节点的事件是由线程池处理的
        } catch (Exception e) {
            e.printStackTrace();
        }
        countDownLatch.await();//创建节点的线程完成创建后，通知主线程继续进行
        service.shutdown();

        //事务操作（curator独有的）
        try {
        	//记录事务中每个操作的结果信息
            Collection<CuratorTransactionResult> resultCollections = curatorFramework
															            		.inTransaction()
															            		.create()	//创建"/trans"节点
															            		.forPath("/trans", "111".getBytes())
															            		.and()
															            		.setData()	//并且修改"/curator"节点
															            		.forPath("/curator", "111".getBytes())
															            		.and()
															            		.commit();	//最后提交事务
            for (CuratorTransactionResult result:resultCollections){
                System.out.println(result.getForPath() + "->" + result.getType());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
