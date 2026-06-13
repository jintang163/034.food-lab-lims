package com.foodlab.form.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodlab.form.entity.FormData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FormDataMapper extends BaseMapper<FormData> {

    @Select("SELECT * FROM form_data WHERE template_id = #{templateId} AND template_version = #{version} AND deleted = 0")
    List<FormData> selectByTemplateIdAndVersion(@Param("templateId") Long templateId, @Param("version") Integer version);

    @Select("SELECT * FROM form_data WHERE offline_id = #{offlineId} AND deleted = 0")
    FormData selectByOfflineId(@Param("offlineId") String offlineId);
}
