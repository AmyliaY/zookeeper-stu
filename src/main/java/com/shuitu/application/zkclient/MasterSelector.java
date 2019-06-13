package com.shuitu.application.zkclient;

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkNodeExistsException;
import org.omg.CORBA.PRIVATE_MEMBER;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
* @author 全恒
* 提供master选举的服务
*/
public class MasterSelector {

    private ZkClient zkClient;

    private final static String MASTER_PATH = "/master"; //需要争抢的master节点

    private IZkDataListener dataListener; //注册节点内容变化

    private UserCenter server;  //其他服务器

    private UserCenter master;  //master节点

    private boolean isRunning = false;

    ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

    public MasterSelector(UserCenter server, ZkClient zkClient) {
        System.out.println("[" + server + "] 去争抢master权限");
        this.server = server;
        this.zkClient = zkClient;

        this.dataListener = new IZkDataListener() {
            @Override
            public void handleDataChange(String s, Object o) throws Exception {

            }

            @Override
            public void handleDataDeleted(String s) throws Exception {
                //节点如果被删除, 重新进行master选举
                chooseMaster();
            }
        };
    }

    public void start(){
        //开始选举，如果当前机器已经启动，则不进行任何处理
        if(!isRunning){
            isRunning = true;
            //注册节点事件，使当前客户端机器监听master节点的删除动作
            zkClient.subscribeDataChanges(MASTER_PATH, dataListener); 
            chooseMaster();
        }
    }

    public void stop(){
        //停止，如果当前机器已经停止，则不进行任何处理
        if(isRunning){
            isRunning = false;
            //关闭定时器
            scheduledExecutorService.shutdown();
            //取消订阅
            zkClient.unsubscribeDataChanges(MASTER_PATH, dataListener);
            releaseMaster();
        }
    }

    //具体选master的实现逻辑
    private void chooseMaster(){
        if(!isRunning){
            System.out.println("当前服务没有启动");
            return ;
        }
        try {
            zkClient.createEphemeral(MASTER_PATH, server);
            master=server; //把server节点赋值给master
            System.out.println(master + "->我现在已经是master，你们要听我的");

            //使用定时器模拟 主服务器出现故障，每2秒释放一次
            scheduledExecutorService.schedule(()->{
                releaseMaster();//释放锁(模拟故障的发生)
            }, 2, TimeUnit.SECONDS);
        } catch (ZkNodeExistsException e){
            //表示master已经存在
            UserCenter userCenter = zkClient.readData(MASTER_PATH, true);
            if(userCenter == null) {
                System.out.println("启动操作：");
                chooseMaster(); //再次获取master
            } else {
                master = userCenter;
            }
        }
    }

    //释放锁(模拟故障的发生)
    private void releaseMaster(){
        //判断当前是不是master，只有master才需要释放
        if(checkMaster()){
            zkClient.delete(MASTER_PATH); //删除
        }
    }

    //判断当前的server是不是master
    private boolean checkMaster(){
        UserCenter userCenter = zkClient.readData(MASTER_PATH);
        if(userCenter.getMc_name().equals(server.getMc_name())){
            master = userCenter;
            return true;
        }
        return false;
    }
}
