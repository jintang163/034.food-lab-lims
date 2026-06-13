package com.foodlab.form.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodlab.form.entity.FormTemplateVersion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FormTemplateVersionMapper extends BaseMapper<FormTemplateVersion> {

    @Select("SELECT * FROM form_template_version WHERE template_id = #{templateId} AND deleted = 0 ORDER BY version DESC")
    List<FormTemplateVersion> selectByTemplateId(@Param("templateId") Long templateId);

    @Select("SELECT * FROM form_template_version WHERE template_id = #{templateId} AND version = #{version} AND deleted = 0")
    FormTemplateVersion selectByTemplateIdAndVersion(@Param("templateId") Long templateId, @Param("version") Integer version);

    @Select("SELECT MAX(version) FROM form_template_version WHERE template_id = #{templateId} AND deleted = 0")
    Integer selectMaxVersionByTemplateId(@Param("templateId") Long templateId);
}
