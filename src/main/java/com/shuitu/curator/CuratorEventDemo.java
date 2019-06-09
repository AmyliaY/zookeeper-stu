package com.shuitu.curator;

import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.zookeeper.CreateMode;
import org.junit.Test;

/**
* @author 全恒
*/
public class CuratorEventDemo {

    /**
     * Curator提供了三种watcher来做节点的监听
     * PatchChildrenCache   监视一个路径下子节点的创建、删除、节点数据更新
     * NodeCache   监视一个节点的创建、更新、删除
     * TreeCache   PathCache与NodeCache的合体（监视路径下的创建、更新、删除事件），缓存路径下的所有子节点的数据
     */

    @SuppressWarnings("resource")
	public static void main(String[] args) throws Exception {
        CuratorFramework curatorFramework = CuratorClientUtils.getInstance();

        //NodeCache，监控节点"/curator"的变化
        NodeCache cache = new NodeCache(curatorFramework, "/curator", false);//false表示 缓存的数据不进行压缩
        cache.start(true);
        cache.getListenable().addListener(()-> System.out.println("节点数据发生变化,变化后的结果："
        	+ new String(cache.getCurrentData().getData())));
        curatorFramework.setData().forPath("/curator", "艾米莉亚".getBytes());

        //PatchChildrenCache，监控"/event"下面子节点的变化
        PathChildrenCache cache1 = new PathChildrenCache(curatorFramework, "/event", true);//true表示对数据进行缓存
        cache1.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
        cache1.getListenable().addListener((curatorFramework1, pathChildrenCacheEvent)->{
            switch (pathChildrenCacheEvent.getType()){
                case CHILD_ADDED:
                    System.out.println("增加子节点");
                    break;
                case CHILD_REMOVED:
                    System.out.println("删除子节点");
                    break;
                case CHILD_UPDATED:
                    System.out.println("修改子节点");
                    break;
                default:
                	break;
            }
        });

        //创建节点
        curatorFramework.create().withMode(CreateMode.PERSISTENT).forPath("/event", "event".getBytes());
        TimeUnit.SECONDS.sleep(1);
        System.out.println("1");
        //创建子节点
        curatorFramework.create().withMode(CreateMode.EPHEMERAL).forPath("/event/event1", "1".getBytes());
        TimeUnit.SECONDS.sleep(1);
        System.out.println("2");
        //修改子节点
        curatorFramework.setData().forPath("/event/event1", "222".getBytes());
        TimeUnit.SECONDS.sleep(1);
        System.out.println("3");
        //删除子节点
        curatorFramework.delete().forPath("/event/event1");
        System.out.println("4");

        System.in.read();//不让程序这么早就结束
    }
    
    @Test
	public void test() throws Exception{
		CuratorFramework curatorFramework = CuratorClientUtils.getInstance();

        /**
         * 节点变化NodeCache
         */
		//PatchChildrenCache，监控"/event"下面子节点的变化
        PathChildrenCache cache1 = new PathChildrenCache(curatorFramework, "/event", true);//true表示对数据进行缓存
        cache1.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
        cache1.getListenable().addListener((curatorFramework1, pathChildrenCacheEvent)->{
            switch (pathChildrenCacheEvent.getType()){
                case CHILD_ADDED:
                    System.out.println("增加子节点");
                    break;
                case CHILD_REMOVED:
                    System.out.println("删除子节点");
                    break;
                case CHILD_UPDATED:
                    System.out.println("修改子节点");
                    break;
                default:
                	break;
            }
        });

        //创建节点
        curatorFramework.create().withMode(CreateMode.PERSISTENT).forPath("/event", "event".getBytes());
        TimeUnit.SECONDS.sleep(1);
        //创建子节点
        curatorFramework.create().withMode(CreateMode.EPHEMERAL).forPath("/event/event1", "1".getBytes());
        TimeUnit.SECONDS.sleep(1);
        //修改子节点
        curatorFramework.setData().forPath("/event/event1", "222".getBytes());
        TimeUnit.SECONDS.sleep(1);
        //删除子节点
        curatorFramework.delete().forPath("/event/event1");

        System.in.read();//不让程序这么早就结束
	}
}
