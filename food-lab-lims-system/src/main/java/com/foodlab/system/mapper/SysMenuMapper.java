package com.foodlab.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodlab.system.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    List<SysMenu> selectMenusByUserId(@Param("userId") Long userId);
}
