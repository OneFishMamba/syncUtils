package com.liuyan.onefish.controller;

import com.liuyan.onefish.dao.UserMapper;
import com.liuyan.onefish.entity.UserEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户控制器类
 * @author Administrator
 */
@RestController
@RequestMapping("/user")
public class UserController {
    //开启slf4j日志，注意引入的包是org.slf4j.*
    private final static Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserMapper userMapper;

   @RequestMapping(value = "/alreadyDeleteIds",method = RequestMethod.GET)
    public List<Integer> alreadyDeleteIds(){
       return userMapper.alreadyDeletedIds();
   }
   @RequestMapping(value = "/AllUsers",method = RequestMethod.GET)
    public List<UserEntity> allUsers(){
       return userMapper.selectAll();
   }
}
