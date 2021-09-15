package com.liuyan.onefish.serviceImpl;

import com.liuyan.onefish.service.IBaseService;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @author Administrator
 * 通用mapper服务实现类
 */
public abstract class BaseService<T> implements IBaseService<T> {
    @Autowired
    protected Mapper<T> mapper;

    public Mapper<T> getMapper(){return mapper;}

    @Override
    public T selectByKey(Object key){
        return mapper.selectByPrimaryKey(key);
    }
    @Override
    public int save(T entity){
        return mapper.insert(entity);
    }
    @Override
    public int delete(Object key){
        return mapper.deleteByPrimaryKey(key);
    }
    @Override
    public int updateAll(T entity){
        return mapper.updateByPrimaryKey(entity);
    }
    @Override
    public int updateNotNull(T entity){
        return mapper.updateByPrimaryKeySelective(entity);
    }
    @Override
    public List<T> selectByExample(Object example){
        return mapper.selectByExample(example);
    }
    @Override
    public List<T> select(T entity){
        return mapper.select(entity);
    }
}
