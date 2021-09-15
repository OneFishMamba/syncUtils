package com.liuyan.onefish.service;

import com.liuyan.onefish.entity.UserEntity;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 用户服务接口
 * @author Administrator
 */
@Component
public interface IUserService extends IBaseService<UserEntity>{
     void sync(List<UserEntity> userlist);
}
