package com.liuyan.onefish.service;

import java.util.List;

/**
 * @author Administrator
 * 通用mapper服务基础接口（7）
 */
public interface IBaseService<T>{
    /**
     * 通过主键查询记录
     * @param key 主键
     * @return T
     */
    T selectByKey(Object key);

    /**
     * 保存一个实体，null的属性值也会保存，不会使用数据库的默认值
     * @param entity 实体
     * @return int
     */
    int save(T entity);

    /**
     * 根据主键删除记录
     * @param key 主键
     * @return int
     */
    int delete(Object key);

    /**
     * 根据主键更新实体的全部属性,null值也会被更新
     * @param entity 实体
     * @return int
     */
    int updateAll(T entity);

    /**
     * 根据主键更新属性不为null的值
     * @param entity 实体
     * @return int
     */
    int updateNotNull(T entity);

    /**
     * 通过查询条件查询记录
     * @param example 查询条件
     * @return list
     */
    List<T> selectByExample(Object example);

    /**
     * 通过实体中的属性值进行查询，查询条件是等号
     * @param entity 实体
     * @return list
     */
    List<T> select(T entity);
}
