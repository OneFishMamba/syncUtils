package com.liuyan.onefish.Timer;

import com.liuyan.onefish.dao.SysparamsMapper;
import com.liuyan.onefish.dao.SysparamsMapper2;
import com.liuyan.onefish.dao.UserMapper;
import com.liuyan.onefish.entity.SysparamsEntity;
import com.liuyan.onefish.entity.SysparamsEntity2;
import com.liuyan.onefish.entity.UserEntity;
import com.liuyan.onefish.entity.UserEntity2;
import com.liuyan.onefish.serviceImpl.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 数据工具类
 * 注意：数据处理性能与cpu核数、磁盘io等因素有关
 * @author Administrator
 */
@Component
public class DataUtil {
    private final static Logger logger = LoggerFactory.getLogger(DataUtil.class);
    private final Lock usersDataLock = new ReentrantLock();
    private final Lock testDataLock = new ReentrantLock();
    private static DataUtil dataUtil;

    @Autowired
    UserService userService;
    @Autowired
    UserMapper userMapper;
    @Autowired
    SysparamsMapper sysparamsMapper;
    @Autowired
    SysparamsMapper2 sysparamsMapper2;
    /**
     * 获取实例
     * @return DataUtil
     */
    public synchronized static DataUtil getInstance(){
        if(dataUtil == null){
            dataUtil = new DataUtil();
        }
        return dataUtil;
    }
    /**
     * 同步数据处理主线程
     */
    private class UserDataHandler extends Thread{
        @Override
        public void run(){
            try {
                usersDataLock.lock();
                syncUserData();

            }catch (Exception e){
                logger.error("同步用户数据出现异常！",e);
            }finally {
                usersDataLock.unlock();
            }
        }
    }

    /**
     * 测试数据处理主线程
     */
    private class TestDataHandler extends Thread{
        @Override
        public void run(){
            try {
                testDataLock.lock();
                testData();

            }catch (Exception e){
                logger.error("测试数据出现异常！",e);
            }finally {
                testDataLock.unlock();
            }
        }
    }
    /**
     *  同步用户数据主线程
     */
    public void syncTread(){
        UserDataHandler userDataHandler = new UserDataHandler();
        userDataHandler.start();
    }
    /**
     * 模拟测试数据主线程
     */
    public void testThread(){
        TestDataHandler testDataHandler = new TestDataHandler();
        testDataHandler.start();
    }
    /**
     * 同步数据业务逻辑
     */
    public synchronized void syncUserData(){
        //根据主库、从库的用户版本判断是否同步
        Date startTime = new Date();
        SysparamsEntity sysparams =  sysparamsMapper.selectByPrimaryKey("userVersion");
        String userVersion = sysparams.getCValue();
        logger.info("主库当前用户版本号:" + userVersion);
        SysparamsEntity2 sysparams2 =  sysparamsMapper2.selectByPrimaryKey("userVersion");
        String userVersion2 = sysparams2.getCValue();
        logger.info("从库当前用户版本号:" + userVersion2);
        if (userVersion.equals(userVersion2)){
            return;
        }
        //针对百万级数据量的查询操作存在问题: Exception in thread "Thread-xxx" java.lang.OutOfMemoryError: GC overhead limit exceeded
        List<UserEntity> list = userMapper.selectAll(); //select待优化
        userService.sync(list);
        //同步完之后对应更新从库用户版本
        sysparams2.setCValue(userVersion);
        sysparamsMapper2.updateByPrimaryKeySelective(sysparams2);
        Date endTime = new Date();
        Long spend = endTime.getTime()-startTime.getTime();
        logger.info("同步用户数据主线程所花费的时间为:" + spend + "ms");
    }

    /**
     * 制造测试数据业务逻辑  --数据量控制在1万左右
     */
    public synchronized void testData(){
        //模拟实际生产环境制造测试数据
        Date startTime = new Date();
        Random random = new Random();
        List<Integer> alIds = userMapper.alreadyDeletedIds();
        logger.info("模拟测试数据操作开始...");
        SysparamsEntity sysparams =  sysparamsMapper.selectByPrimaryKey("userVersion");
        String userVersion = sysparams.getCValue();
        logger.info("当前用户版本号:" + userVersion);
        logger.info("新增操作...");
        Integer uVersion =  Integer.parseInt(userVersion);
        //测试数据批量插入  --自增主键
        List<UserEntity> addList = new ArrayList<>();
        for (int i = 0; i < 100000; i++) {
            UserEntity user = new UserEntity();
            user.setUserName(UUID.randomUUID().toString());
            user.setAge(i);
            user.setAddress(UUID.randomUUID().toString());
            user.setCollege(UUID.randomUUID().toString());
            user.setHobby(UUID.randomUUID().toString());
            user.setProfession(UUID.randomUUID().toString());
            user.setSkill(UUID.randomUUID().toString());
            user.setCreateTime(new Date());
            user.setUpdateTime(new Date());
            user.setIsDeleted(0);

            addList.add(user);
            //单条SQL插入
//            userMapper.insert(user);
            //每新增一条数据,版本号对应+1
//            uVersion++;
//            sysparams.setCValue(uVersion.toString());
//            sysparamsMapper.updateByPrimaryKeySelective(sysparams);
        }
        logger.info("新增数据量的大小:" + addList.size());
        //批量插入,相比于单条循环插入性能提升10倍以上
        //批量操作，可能存在单次解析数据包packet过大的问题,通过数据分片或调大max_allowed_packet参数解决;事务问题,若整个事务中有一条数据出现错误,那么整个事务将全部回滚数据
        int addListCount = (int)Math.ceil((double)addList.size()/800);//向上取整
        logger.info("addListCount = " + addListCount);
        List<List<UserEntity>> list = Lists.partition(addList,addListCount);
        list.forEach((e)->{
            if(!e.isEmpty()){
                userMapper.insertBatch(e);
            }
        });
        //调整数据库引擎默认参数max_allowed_packet的大小,set global max_allowed_packet = 64*1024*1024;
//        userMapper.insertBatch(addList);

        //更新用户数据版本
        uVersion += 100000;
//        sysparams.setCValue(uVersion.toString());
//        sysparamsMapper.updateByPrimaryKeySelective(sysparams);
        logger.info("新增操作完成!");

        //测试数据批量更新(随机)
        logger.info("更新操作...");
        Integer id = 0;
        Integer count = userMapper.count();
        List<UserEntity> updList = new ArrayList<>();
        for (int i = 0; i < 2000; i++) {
            UserEntity user = new UserEntity();
            id = random.nextInt(count)+1;
            if(!alIds.contains(id)){
                user.setId(id);
                user.setUserName(UUID.randomUUID().toString());
                user.setAddress(UUID.randomUUID().toString());
                user.setSkill(UUID.randomUUID().toString());
                user.setUpdateTime(new Date());

                updList.add(user);
            }
//            userMapper.update(user);
            //每更新一条记录对应用户版本号+1
//            uVersion++;
//            sysparams.setCValue(uVersion.toString());
//            sysparamsMapper.updateByPrimaryKeySelective(sysparams);
        }
        logger.info("更新的数据量大小:" + updList.size());
        //批量更新
        int updListCount = (int)Math.ceil((double)updList.size()/800);//向上取整
        logger.info("updListCount = " + updListCount);
        List<List<UserEntity>> list2 = Lists.partition(updList,updListCount);
        list2.forEach((e)->{
            if(!e.isEmpty()){
                userMapper.updateBatch(e);
            }
        });
//        userMapper.updateBatch(updList);
        uVersion += 2000;
        logger.info("更新操作完成！");

//        //测试数据批量删除(随机性)  --已删除的过滤掉
        logger.info("删除操作...");
        List<Integer> delIds = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            /**
             * 取[m,n]的随机数
             * random.nextInt(n-m+1)+m
             * 产生[1,count]的随机数
             */
            id = random.nextInt(count)+1;
            if(!alIds.contains(id)){
                delIds.add(id);
            }
        }
        logger.info("待删除用户的ids:" + delIds);
        logger.info("删除的数据量大小:" + delIds.size());
        //批量删除
        userMapper.deleteByIds(delIds);
        logger.info("删除操作完成!");
        //删除操作后更新用户版本，每删除一条记录对应的用户版本号+1
        uVersion += 50;
//        sysparams.setCValue(uVersion.toString());
//        sysparamsMapper.updateByPrimaryKeySelective(sysparams);

        sysparams.setCValue(uVersion.toString());
        sysparamsMapper.updateByPrimaryKeySelective(sysparams);
        Date endTime = new Date();
        Long spend = endTime.getTime()-startTime.getTime();
        logger.info("模拟测试环境创建" + (addList.size() + delIds.size() + updList.size()) + "条测试数据所花费的时间为:" + spend + "ms");
    }
}
