package com.foodlab.detect.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodlab.detect.entity.DetectItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DetectItemMapper extends BaseMapper<DetectItem> {

    @Select("SELECT di.*, dic.category_name FROM detect_item di " +
            "LEFT JOIN detect_item_category dic ON di.category_id = dic.id " +
            "WHERE di.id = #{id} AND di.deleted = 0")
    DetectItem selectDetailById(@Param("id") Long id);

    @Select("SELECT di.*, dic.category_name FROM detect_item di " +
            "LEFT JOIN detect_item_category dic ON di.category_id = dic.id " +
            "WHERE di.deleted = 0 AND di.status = '0'")
    List<DetectItem> selectAllWithCategory();

    @Select("SELECT di.*, dic.category_name FROM detect_item di " +
            "LEFT JOIN detect_item_category dic ON di.category_id = dic.id " +
            "WHERE di.category_id = #{categoryId} AND di.deleted = 0 AND di.status = '0'")
    List<DetectItem> selectByCategoryId(@Param("categoryId") Long categoryId);

    @Select("SELECT item_name FROM detect_item WHERE id = #{id} AND deleted = 0")
    String selectItemNameById(@Param("id") Long id);
}
