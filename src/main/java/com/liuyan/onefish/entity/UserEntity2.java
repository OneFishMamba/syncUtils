package com.liuyan.onefish.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * 从库用户实体
 * @author Administrator
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties
@Table(name = "user2")
public class UserEntity2 {
    @Id
    private Integer id;
    @Column(name = "userName")
    private String userName;
    @Column(name = "age")
    private Integer age;
    @Column(name = "address")
    private String address;
    @Column(name = "college")
    private String college;
    @Column(name = "profession")
    private String profession;
    @Column(name = "hobby")
    private String hobby;
    @Column(name = "skill")
    private String skill;
    @Column(name = "createTime")
    private Date createTime;
    @Column(name = "updateTime")
    private Date updateTime;
}
