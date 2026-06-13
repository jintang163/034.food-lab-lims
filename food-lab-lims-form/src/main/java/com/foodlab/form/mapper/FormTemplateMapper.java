package com.foodlab.form.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodlab.form.entity.FormTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface FormTemplateMapper extends BaseMapper<FormTemplate> {

    @Select("SELECT * FROM form_template WHERE detect_item_id = #{detectItemId} AND is_current = true AND deleted = 0")
    FormTemplate selectCurrentByDetectItemId(@Param("detectItemId") Long detectItemId);

    @Update("UPDATE form_template SET is_current = false WHERE detect_item_id = #{detectItemId} AND deleted = 0")
    void clearCurrentFlagByDetectItemId(@Param("detectItemId") Long detectItemId);
}
