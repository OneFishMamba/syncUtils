package com.liuyan.onefish.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

/**
 * 主库用户实体
 * @author Administrator
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Component
@Table(name = "user")
//JPA
public class UserEntity {
    @Id
    @Column(name = "id")
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
    @Column(name = "isDeleted")
    private Integer isDeleted;
}
