package com.foodlab.form.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodlab.form.entity.FormFieldMapping;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FormFieldMappingMapper extends BaseMapper<FormFieldMapping> {

    @Select("SELECT * FROM form_field_mapping WHERE template_id = #{templateId} AND deleted = 0 ORDER BY sort_order ASC")
    List<FormFieldMapping> selectByTemplateId(@Param("templateId") Long templateId);

    @Select("SELECT * FROM form_field_mapping WHERE template_id = #{templateId} AND is_result_field = true AND deleted = 0 ORDER BY sort_order ASC")
    List<FormFieldMapping> selectResultFieldsByTemplateId(@Param("templateId") Long templateId);
}
