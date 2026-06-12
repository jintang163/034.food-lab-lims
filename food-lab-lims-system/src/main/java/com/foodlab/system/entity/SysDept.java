package com.foodlab.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.foodlab.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dept")
public class SysDept extends BaseEntity {

    private Long parentId;

    private String deptName;

    private String deptCode;

    private Integer sort;

    private Integer status;

    private String leader;

    private String phone;

    private String email;

    private String remark;
}
