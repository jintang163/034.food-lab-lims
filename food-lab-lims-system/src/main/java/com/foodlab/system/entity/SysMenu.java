package com.foodlab.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.foodlab.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_menu")
public class SysMenu extends BaseEntity {

    private Long parentId;

    private String menuName;

    private String menuCode;

    private Integer menuType;

    private String path;

    private String component;

    private String icon;

    private Integer sort;

    private Integer status;

    private String permission;
}
