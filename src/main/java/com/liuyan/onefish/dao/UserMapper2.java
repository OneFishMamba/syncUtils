package com.liuyan.onefish.dao;

import com.liuyan.onefish.entity.UserEntity2;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @author Administrator
 */
@Component
public interface UserMapper2 extends Mapper<UserEntity2> {
    List<UserEntity2> selectAll2();
    int deleteByIds2(List<Integer> delIds);
    int insertBatch(List<UserEntity2> userList);
    int updateBatch(List<UserEntity2> userList);
}
