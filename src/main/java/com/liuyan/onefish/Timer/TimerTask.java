package com.liuyan.onefish.Timer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定时任务
 * @author Administrator
 */
@Component
public class TimerTask {
    @Autowired
    DataUtil dataUtil;
    /**
     * 模拟生产环境制造测试数据任务
     * 1、fixedDelay 任务的时长（当被执行的任务时长超过该时长系统会做怎样的处理）
     * 2、initialDelay 延迟启动时长（初始化对象、集合、容器、缓存刷新等）
     */
    @Scheduled(fixedDelay = 40000,initialDelay = 1000)
    public void testOp(){
        dataUtil.testThread();
    }

    /**
     * 数据同步操作任务
     */
    @Scheduled(fixedDelay = 20000,initialDelay = 2000)
    public void syncOp(){
        dataUtil.syncTread();
    }
}
