package com.liuyan.onefish.dao;

import com.liuyan.onefish.entity.UserEntity;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

/**
 * 用户mapper类
 * @author Administrator
 */
@Repository("UserMapper")
public interface UserMapper extends Mapper<UserEntity> {
    int deleteByIds(List<Integer> delIds);
    List<Integer> alreadyDeletedIds();
    Integer update(UserEntity user);
    Integer count();
    List<UserEntity> All();
    int insertBatch(List<UserEntity> userList);
    int updateBatch(List<UserEntity> userList);
}
