package com.liuyan.onefish.serviceImpl;

import com.google.common.collect.Lists;
import com.liuyan.onefish.dao.UserMapper2;
import com.liuyan.onefish.entity.UserEntity;
import com.liuyan.onefish.entity.UserEntity2;
import com.liuyan.onefish.service.IUserService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 用户服务实现类
 */
@Service("UserService")
@Component
public class UserService extends BaseService<UserEntity> implements IUserService {
    @Autowired
    UserMapper2 userMapper2;

    private final static Logger logger = LoggerFactory.getLogger(UserService.class);
    private static ExecutorService pool;
    /**
     * 通用定义方式 -- 接口类对象 = new 实现类构造方法
     */
    private final Lock addLock = new ReentrantLock();
    private final Lock updLock = new ReentrantLock();
    private final Lock delLock = new ReentrantLock();
    private static final AtomicInteger threadCount  = new AtomicInteger(0);
    private  CountDownLatch latch = new CountDownLatch(threadCount.get());

    /**
     * 线程池进行线程管理
     * 每个线程加锁可以保证线程安全性
     */
//    private class AddDataHandler implements Runnable{
//        private List<UserEntity2> addUser = new ArrayList<>();
//        public AddDataHandler(List<UserEntity2> userList){
//            this.addUser = userList;
//        }
//        @Override
//        public void run(){
//            try {
//                addLock.lock();
//                for (UserEntity2 user:addUser) {
//                    userMapper2.insert(user);
//                }
//            }catch (Exception e){
//                logger.error("执行同步新增数据线程时出现错误!",e);
//            }finally {
//                latch.countDown();
//                addLock.unlock();
//            }
//        }
//    }
    private class AddDataHandler extends Thread{
        private List<UserEntity2> addUser = new ArrayList<>();
        public AddDataHandler(List<UserEntity2> userList){
            this.addUser = userList;
        }

        @Override
        public void run(){
            try {
                /**
                 * 不加锁异步执行
                 */
//                addLock.lock();
                logger.info("当前线程为:" + Thread.currentThread().getName());
                /**
                 * 单条SQL插入
                 */
//                for (UserEntity2 user:addUser) {
//                    userMapper2.insert(user);
//                }
                /**
                 * 批量插入,可能存在单次解析数据包packet过大的问题,通过数据分片或调大max_allowed_packet参数解决
                 */
                int addListCount = (int)Math.ceil((double)addUser.size()/800);
//                logger.info("待同步新增的数据分片大小 = " + addListCount);
                List<List<UserEntity2>> list = Lists.partition(addUser,addListCount);
                list.forEach((e)->{
                    if(!e.isEmpty()){
                        userMapper2.insertBatch(e);
                    }
                });
                //调整数据库引擎默认参数max_allowed_packet的大小,set global max_allowed_packet = 64*1024*1024;
//                userMapper2.insertBatch(addUser);
            }catch (Exception e){
                logger.error("执行同步新增数据线程时出现错误!",e);
            }finally {
                latch.countDown();
//                addLock.unlock();
            }
        }
    }

    private class UpdateDataHandler extends Thread{
        private List<UserEntity2> updUser = new ArrayList<>();
        public UpdateDataHandler(List<UserEntity2> updUser){
            this.updUser = updUser;
        }

        @Override
        public void run(){
            try {
                /**
                 * 不加锁异步执行
                 */
//                updLock.lock();
                logger.info("当前线程为:" + Thread.currentThread().getName());
                /**
                 * 单条SQL更新
                 */
//                for (UserEntity2 user:updUser) {
//                    userMapper2.updateByPrimaryKeySelective(user);
//                }

                int updListCount = (int)Math.ceil((double)updUser.size()/800);//向上取整
//                logger.info("待同步更新的数据分片大小 = " + updListCount);
                List<List<UserEntity2>> list = Lists.partition(updUser,updListCount);
                list.forEach((e)->{
                    if(!e.isEmpty()){
                        userMapper2.updateBatch(e);
                    }
                });
                //调整数据库引擎默认参数max_allowed_packet的大小,set global max_allowed_packet = 64*1024*1024;
                /**
                 * 批量更新,可能存在单次解析数据包packet过大的问题,通过数据分片或调大max_allowed_packet参数解决
                 */
//                userMapper2.updateBatch(updUser);
            }catch (Exception e){
                logger.error("执行同步更新数据线程时出现错误!",e);
            }finally {
                latch.countDown();
//                updLock.unlock();
            }
        }
    }

    private class DeleteDataHandler extends Thread{
        private List<Integer> delIds = new ArrayList<>();
        public DeleteDataHandler(List<Integer> delIds){
            this.delIds = delIds;
        }

        @Override
        public void run(){
            try {
                /**
                 * 不加锁异步执行
                 */
//                delLock.lock();
                logger.info("当前线程为:" + Thread.currentThread().getName());
                /**
                 * 单条SQL删除
                 */
//                for (Integer id : delIds) {
//                    userMapper2.deleteByPrimaryKey(id);
//                }
                /**
                 * 批量删除
                 */
                userMapper2.deleteByIds2(delIds);
            }catch (Exception e){
                logger.error("执行同步删除数据线程时出现错误!",e);
            }finally {
                latch.countDown();
//                delLock.unlock();
            }
        }
    }
    @Override
    public void sync(List<UserEntity> userlist) {
        //主库待同步数据处理
        try {
            Map<Integer,Integer> uMap = new HashMap<>();
            List<UserEntity> uList = new ArrayList<>();
            //去重
            for (UserEntity user: userlist) {
                if(!uMap.containsKey(user.getId())){
                    uList.add(user);
                    uMap.put(user.getId(),user.getId());
                }
            }

            List<Integer> delIds = new ArrayList<>();
            List<UserEntity2> updUser = new ArrayList<>();
            List<UserEntity2> addUser = new ArrayList<>();

            //从库数据
            List<UserEntity2> list2 = userMapper2.selectAll2();
            Map<Integer,UserEntity2> uMap2 = new HashMap<>();
            for (UserEntity2 user2:list2){
                uMap2.put(user2.getId(),user2);
            }
            for(UserEntity user:uList){
                //判断待删除(包含已删除)
                if("1".equals(user.getIsDeleted().toString())){
                    if(uMap2.containsKey(user.getId())){
                        delIds.add(user.getId());
                    }
                    continue;
                }
                //javabean字段对应关系
                UserEntity2 user2 = new UserEntity2();
                user2.setId(user.getId());
                user2.setUserName(user.getUserName());
                user2.setAddress(user.getAddress());
                user2.setAge(user.getAge());
                user2.setCollege(user.getCollege());
                user2.setSkill(user.getSkill());
                user2.setHobby(user.getHobby());
                user2.setProfession(user.getProfession());
                user2.setCreateTime(user.getCreateTime());
                user2.setUpdateTime(user.getUpdateTime());

                //判断待更新(包含未更新)
                if(uMap2.containsKey(user.getId())){
                    if(isupdate(user2,uMap2.get(user.getId()))){
                        updUser.add(user2);
                    }
                    continue;
                }
                //判断待增加
                if(!uMap2.containsKey(user.getId())){
                    addUser.add(user2);
                }

            }
            /**
             * 单线程批量同步数据
             */
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        Date startTime = new Date();
//
//        //同步删除数据
//        if(!delIds.isEmpty()){
//            userMapper2.deleteByIds2(delIds);
//        }
//        //同步新增数据
//        for (UserEntity2 user:addUser) {
//           userMapper2.insert(user);
//        }
            //同步更新数据
//        for (UserEntity2 user: updUser) {
//            userMapper2.updateByPrimaryKey(user);
//        }
//        //批量更新
//        userMapper2.updateBatch(updUser);
//        Date endTime = new Date();
//        Long spend = endTime.getTime()-startTime.getTime();
//        logger.info("批量SQL部分采用单线程处理方式所花费的时间为:" + spend + "ms");

            /**
             * 多线程批量同步数据  --线程池
             */
            logger.info("子线程开始执行...");
            Date startTime = new Date();
            pool = new ThreadPoolExecutor(8,15,0, TimeUnit.MILLISECONDS,new ArrayBlockingQueue<>(10),Executors.defaultThreadFactory(),new ThreadPoolExecutor.AbortPolicy());

            logger.info("待同步新增的数据量大小:" + addUser.size());
            logger.info("待同步更新的数据量大小:" + updUser.size());
            logger.info("待同步删除的数据量大小:" + delIds.size());
            for (int i = 0; i < 5; i++) {
                if(!addUser.isEmpty()){
                    if(i == 0){
                        threadCount.incrementAndGet();
                        pool.execute(new AddDataHandler(addUser.subList(0,3334)));
                    }
                    if(i == 1){
                        threadCount.incrementAndGet();
                        pool.execute(new AddDataHandler(addUser.subList(3334,6667)));
                    }
                    if(i == 2){
                        threadCount.incrementAndGet();
                        pool.execute(new AddDataHandler(addUser.subList(6667,9950)));
                    }
                }
                if(!updUser.isEmpty()){
                    if(i == 3){
                        threadCount.incrementAndGet();
                        pool.execute(new UpdateDataHandler(updUser));
                    }
                }
                if(!delIds.isEmpty()){
                    if(i == 4){
                        threadCount.incrementAndGet();
                        pool.execute(new DeleteDataHandler(delIds));
                    }
                }
            }
            //回收线程，释放线程占用的资源
            pool.shutdown();

            //多线程批量处理
//            logger.info("待同步新增的数据量大小:" + addUser.size());
//            logger.info("待同步更新的数据量大小:" + updUser.size());
//            logger.info("待同步删除的数据量大小:" + delIds.size());
//            for (int i = 0; i < 5; i++) {
//                if(!addUser.isEmpty()){
//                    if(i == 0){
//                        threadCount.incrementAndGet();
//                        AddDataHandler addDataHandler = new AddDataHandler(addUser.subList(0,3334));
//                        addDataHandler.start();
//                    }
//                    if(i == 1){
//                        threadCount.incrementAndGet();
//                        AddDataHandler addDataHandler = new AddDataHandler(addUser.subList(3334,6667));
//                        addDataHandler.start();
//                    }
//                    if(i == 2){
//                        threadCount.incrementAndGet();
//                        AddDataHandler addDataHandler = new AddDataHandler(addUser.subList(6667,addUser.size()));
//                        addDataHandler.start();
//                    }
//                }
//               if(!updUser.isEmpty()){
//                   if(i == 3){
//                       threadCount.incrementAndGet();
//                       UpdateDataHandler updateDataHandler = new UpdateDataHandler(updUser);
//                       updateDataHandler.start();
//                   }
//               }
//                if(!delIds.isEmpty()){
//                    if(i == 4){
//                        threadCount.incrementAndGet();
//                        DeleteDataHandler deleteDataHandler = new DeleteDataHandler(delIds);
//                        deleteDataHandler.start();
//                    }
//                }
//            }

            latch.await();
            logger.info("启动的线程数counter:" + threadCount.get());
            logger.info("子线程执行完毕!");
            Date endTime = new Date();
            Long spend = endTime.getTime()-startTime.getTime();
            logger.info("批量SQL部分采用多线程处理方式所花费的时间为:" + spend + "ms");

            logger.info("主线程继续执行...");
            logger.info("主线程执行完毕!");
            //刷新threadCount、latch
            threadCount.set(0);
            latch = new CountDownLatch(threadCount.get());
        }catch (InterruptedException e){
            logger.error("同步执行出现异常!",e);
        }

    }

    /**
     * 判断是否更新
     */
    public boolean isupdate(UserEntity2 newuser,UserEntity2 olduser){
        boolean flag = StringUtils.equals(newuser.getUserName(),olduser.getUserName());
        boolean flag2 = StringUtils.equals(newuser.getAddress(),olduser.getAddress());
        boolean flag3 = StringUtils.equals(newuser.getSkill(),olduser.getSkill());
        return !flag || !flag2 || !flag3;
    }
}
