package com.liuyan.onefish.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 主库系统参数实体
 * @author Administrator
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
//JPA
@Table(name = "sysparams")
public class SysparamsEntity {
    @Id
    @Column(name = "c_key")
    private String cKey;
    @Column(name = "c_value")
    private String cValue;
}
